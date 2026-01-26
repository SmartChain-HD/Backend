package com.smartchain.platform.dto.common.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageManifestDto {
    private int totalFiles;
    private List<String> contents;
}
