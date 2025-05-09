package com.diplom.planerservice.Service;

import com.diplom.planerservice.Model.Label;
import com.diplom.planerservice.Repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelService {
    private final LabelRepository labelRepository;

    public List<Label> getLabelsByLogin(String login) {
        return labelRepository.findByLogin(login);
    }

    public Label saveLabel(Label label) {
        return labelRepository.save(label);
    }

    public void deleteLabel(Long id) {
        labelRepository.deleteById(id);
    }

    public List<String> getLabelNamesByIds(Set<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return Collections.emptyList();
        }
        return labelRepository.findByIdIn(labelIds).stream()
                .map(Label::getName)
                .collect(Collectors.toList());
    }
}