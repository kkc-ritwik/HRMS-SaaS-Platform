package com.hrms.helpdesk.csat;

import com.hrms.helpdesk.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/helpdesk/tickets/{id}/csat")
@RequiredArgsConstructor
public class CsatController {

    private final CsatTrigger csat;

    @PostMapping
    public Ticket rate(@PathVariable UUID id,
                        @RequestParam int score,
                        @RequestParam(required = false) String comment) {
        if (score < 1 || score > 5) throw new IllegalArgumentException("Score must be 1-5");
        return csat.recordRating(id, score, comment);
    }
}
