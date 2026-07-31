package org.example.file_api.folder.controller;

import jakarta.validation.Valid;
import org.example.file_api.folder.dto.FolderCreateReqDTO;
import org.example.file_api.folder.dto.FolderRespDTO;
import org.example.file_api.folder.dto.FolderUpdateReqDTO;
import org.example.file_api.folder.service.FolderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public FolderRespDTO createFolder(@Valid @RequestBody FolderCreateReqDTO request) {
        return folderService.createFolder(request);
    }

    @GetMapping("/{id}")
    public FolderRespDTO getFolder(@PathVariable Long id) {
        return folderService.getFolder(id);
    }

    @DeleteMapping("/{id}")
    public void deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);
    }

    @PutMapping("/{id}")
    public FolderRespDTO updateFolder(@PathVariable Long id,
                                      @Valid @RequestBody FolderUpdateReqDTO request) {
        return folderService.updateFolder(id, request);
    }
}
