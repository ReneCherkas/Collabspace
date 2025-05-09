package com.diplom.planerservice.Controllers;

import com.diplom.planerservice.Model.Label;
import com.diplom.planerservice.Service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {
    private final LabelService labelService;

    @GetMapping("/{login}")
    public ResponseEntity<List<Label>> getUserLabels(@PathVariable String login) {
        return ResponseEntity.ok(labelService.getLabelsByLogin(login));
    }

    @PostMapping
    public ResponseEntity<Label> createLabel(@RequestBody Label label) {
        return ResponseEntity.ok(labelService.saveLabel(label));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        labelService.deleteLabel(id);
        return ResponseEntity.noContent().build();
    }
}