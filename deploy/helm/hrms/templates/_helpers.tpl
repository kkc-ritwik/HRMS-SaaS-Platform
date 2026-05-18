{{- define "hrms.commonEnv" -}}
{{- range $k, $v := .Values.env }}
- name: {{ $k }}
  value: {{ $v | quote }}
{{- end }}
- name: DB_USER
  valueFrom: { secretKeyRef: { name: hrms-db, key: username } }
- name: DB_PASSWORD
  valueFrom: { secretKeyRef: { name: hrms-db, key: password } }
- name: JWT_SECRET
  valueFrom: { secretKeyRef: { name: {{ .Values.secrets.jwt }}, key: secret } }
- name: STORAGE_ACCESS_KEY
  valueFrom: { secretKeyRef: { name: {{ .Values.secrets.storage }}, key: accessKey } }
- name: STORAGE_SECRET_KEY
  valueFrom: { secretKeyRef: { name: {{ .Values.secrets.storage }}, key: secretKey } }
- name: SMTP_HOST
  valueFrom: { secretKeyRef: { name: {{ .Values.secrets.smtp }}, key: host, optional: true } }
- name: SMTP_USERNAME
  valueFrom: { secretKeyRef: { name: {{ .Values.secrets.smtp }}, key: username, optional: true } }
- name: SMTP_PASSWORD
  valueFrom: { secretKeyRef: { name: {{ .Values.secrets.smtp }}, key: password, optional: true } }
{{- end -}}
