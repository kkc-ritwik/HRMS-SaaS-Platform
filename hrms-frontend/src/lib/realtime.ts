import { useEffect, useRef, useState } from 'react'
import { config } from './config'

/**
 * Subscribe to a Server-Sent-Events channel. Backend route:
 *   GET /api/notifications/sse/dashboard?channel=...
 *
 * The handler is invoked on each event. Connection auto-reconnects on the browser-default
 * retry interval (the SseEmitter heartbeat fires every 30s).
 */
export function useSse(channel: string | null, handler: (evt: MessageEvent) => void): void {
  const handlerRef = useRef(handler)
  handlerRef.current = handler

  useEffect(() => {
    if (!channel) return
    const token = localStorage.getItem('accessToken')
    const tenant = localStorage.getItem('activeTenant') || config.defaultTenant
    const url = `${config.sseBaseUrl}/api/notifications/sse/dashboard?channel=${encodeURIComponent(channel)}`
      + `&access_token=${encodeURIComponent(token || '')}&tenant=${encodeURIComponent(tenant)}`
    const es = new EventSource(url, { withCredentials: false })
    es.onmessage = e => handlerRef.current(e)
    es.onerror = () => {
      // EventSource auto-reconnects; we just log
      console.warn('[SSE] connection error on', channel)
    }
    return () => es.close()
  }, [channel])
}

/**
 * STOMP-over-WebSocket subscription. Backend mounts STOMP at /ws via SockJS.
 * For simplicity we use a thin raw WebSocket client here — the production app should
 * upgrade to @stomp/stompjs for full STOMP frame parsing.
 */
export type WsMessage<T = unknown> = { type: string; tenantId?: string; payload?: T }

export function useNotificationStream(handler: (msg: WsMessage) => void): { connected: boolean } {
  const handlerRef = useRef(handler)
  handlerRef.current = handler
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    const token = localStorage.getItem('accessToken')
    if (!token) return
    const url = `${config.wsUrl}?token=${encodeURIComponent(token)}`
    let ws: WebSocket | null = null
    let stopped = false

    function open() {
      if (stopped) return
      ws = new WebSocket(url)
      ws.onopen = () => setConnected(true)
      ws.onclose = () => {
        setConnected(false)
        if (!stopped) setTimeout(open, 3000)
      }
      ws.onerror = () => ws?.close()
      ws.onmessage = ev => {
        try {
          const parsed = JSON.parse(ev.data)
          handlerRef.current(parsed)
        } catch {
          /* binary or non-JSON frame */
        }
      }
    }
    open()
    return () => {
      stopped = true
      ws?.close()
    }
  }, [])

  return { connected }
}
