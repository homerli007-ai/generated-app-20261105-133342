package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed}")
    private String allowedRoot;

    public record DeleteRequest(List<String> paths) {}
    public record DeleteResult(String path, boolean deleted, String reason) {}
    public record DeleteResponse(List<DeleteResult> results) {}

    @PostMapping("/delete")
    public ResponseEntity<DeleteResponse> delete(@RequestBody DeleteRequest request) {
        List<DeleteResult> results = new ArrayList<>();
        Path root = Paths.get(allowedRoot).toAbsolutePath().normalize();
        List<String> paths = request.paths() == null ? List.of() : request.paths();
        for (String raw : paths) {
            results.add(handleOne(root, raw));
        }
        return ResponseEntity.ok(new DeleteResponse(results));
    }

    private DeleteResult handleOne(Path root, String raw) {
        if (raw == null || raw.isBlank()) {
            return new DeleteResult(raw, false, "empty path");
        }
        try {
            Path p = Paths.get(raw).toAbsolutePath().normalize();
            if (!p.startsWith(root)) {
                return new DeleteResult(raw, false, "outside allowed root");
            }
            if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
                return new DeleteResult(raw, false, "does not exist");
            }
            if (Files.isSymbolicLink(p)) {
                return new DeleteResult(raw, false, "symlink refused");
            }
            if (!Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
                return new DeleteResult(raw, false, "not a regular file");
            }
            Files.delete(p);
            return new DeleteResult(raw, true, "deleted");
        } catch (IOException e) {
            return new DeleteResult(raw, false, "io error: " + e.getMessage());
        } catch (InvalidPathException e) {
            return new DeleteResult(raw, false, "invalid path");
        }
    }
}
