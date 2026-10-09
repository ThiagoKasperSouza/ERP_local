package com.tks.erplocal.domain.vault.model;

import java.util.Locale;
import java.util.Set;

public enum FileKind {
    SOLIDWORKS,
    INVENTOR,
    CREO,
    STEP,
    DWG,
    PDF,
    CSV,
    SPREADSHEET,
    EBOM,
    OTHER;

    public static FileKind fromFilename(String filename) {
        String ext = "";
        int dot = filename == null ? -1 : filename.lastIndexOf('.');
        if (dot >= 0) {
            ext = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        return switch (ext) {
            case "sldprt", "sldasm", "slddrw" -> SOLIDWORKS;
            case "ipt", "iam", "idw", "ipn" -> INVENTOR;
            case "prt", "asm", "drw" -> CREO;
            case "step", "stp", "iges", "igs" -> STEP;
            case "dwg", "dxf" -> DWG;
            case "pdf" -> PDF;
            case "csv" -> CSV;
            case "xls", "xlsx", "ods" -> SPREADSHEET;
            case "ebom", "json" -> EBOM;
            default -> OTHER;
        };
    }

    public static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "sldprt", "sldasm", "slddrw",
            "ipt", "iam", "idw", "ipn",
            "prt", "asm", "drw",
            "dwg", "dxf", "pdf", "csv", "xls", "xlsx", "ods",
            "ebom", "json", "step", "stp", "iges", "igs");
}
