package com.metron.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.metron.service.FileMonitorService;

import java.util.List;

@RestController
@RequestMapping("/monitor")
public class FileMonitorController {

    @Autowired
    private FileMonitorService fileMonitorService;

    @Operation(summary = "Update monitored file extensions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Extensions updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Extensions updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Invalid file extensions list.")))
    })
    @PostMapping("/extensions")
    public ResponseEntity<String> updateExtensions(@RequestBody List<String> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid file extensions list.");
        }

        for (String ext : extensions) {
        	System.out.println("Received extensions: " + ext);
            if (!ext.startsWith(".")) {
                return ResponseEntity.badRequest().body("Invalid file extension format: " + ext);
            }
        }

        fileMonitorService.setMonitoredExtensions(extensions);
        return ResponseEntity.ok("Extensions updated successfully.");
    }
}
