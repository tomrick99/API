package org.example.file_api.folder.service;

import org.example.file_api.folder.dto.FolderCreateReqDTO;
import org.example.file_api.folder.dto.FolderRespDTO;
import org.example.file_api.folder.dto.FolderUpdateReqDTO;

public interface FolderService {

    public FolderRespDTO createFolder(FolderCreateReqDTO  request);

    public FolderRespDTO getFolder(Long id);

    public FolderRespDTO updateFolder(Long id, FolderUpdateReqDTO  request);

    public void deleteFolder(Long id);
}
