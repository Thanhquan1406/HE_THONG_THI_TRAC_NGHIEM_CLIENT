package com.thitracnghiem.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitracnghiem.client.submit.AnswerSubmit;
import com.thitracnghiem.client.submit.SubmitRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lưu tạm bài làm ra file JSON để tránh mất dữ liệu khi app bị tắt.
 */
public class DraftService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path draftDir;

    public DraftService() {
        String home = System.getProperty("user.home");
        this.draftDir = Path.of(home, ".thi-trac-nghiem", "drafts");
    }

    public Path saveDraft(Long studentId, Long examId, Map<Long, Set<Long>> selectedAnswersByQuestionId) throws IOException {
        Files.createDirectories(draftDir);

        SubmitRequest req = new SubmitRequest();
        req.setStudentId(studentId);
        req.setExamId(examId);

        List<AnswerSubmit> answers = selectedAnswersByQuestionId.entrySet().stream()
                .map(e -> new AnswerSubmit(
                        e.getKey(),
                        e.getValue() == null ? List.of() : e.getValue().stream().collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        req.setAnswers(answers);

        String filename = "draft_student_" + studentId + "_exam_" + examId + ".json";
        Path file = draftDir.resolve(filename);

        // Ghi atomically: write temp rồi rename
        Path tmp = draftDir.resolve(filename + "." + Instant.now().toEpochMilli() + ".tmp");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), req);

        File target = file.toFile();
        File temp = tmp.toFile();
        if (target.exists() && !target.delete()) {
            // fallback: nếu không xoá được thì vẫn cố ghi đè trực tiếp
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(target, req);
            temp.delete();
            return file;
        }

        if (!temp.renameTo(target)) {
            // fallback: copy nếu rename fail (Windows có thể lock)
            Files.copy(tmp, file);
            Files.deleteIfExists(tmp);
        }

        return file;
    }
}

