package com.tks.erplocal.domain.permissions.model;

public enum PermissionsEnum {
    CAN_USE_VAULT("CanUseVault", "Acesso ao cofre"),
    CAN_VERSION("CanVersion", "Acesso ao fluxo de versionamento"),
    CAN_APPROVE("CanApprove", "Acesso aos workflows"),
    CAN_SCRIPT("CanScript", "Acesso aos roteiros"),
    CAN_TIME("CanTime", "Acesso aos tempos e métodos"),
    CAN_MACHINE("CanMachine", "Acesso ao gerenciamento de máquinas"),
    CAN_INSTRUCT("CanInstruct", "Acesso as instruções"),
    CAN_REGISTER("CanRegister", "Acesso ao cadastro mestre"),
    CAN_MANAGE_MBOM("CanManageMbom", "Acesso ao gerenciamento de MBOM"),
    CAN_MANAGE_ENG("CanManageEng", "Gerenciamento das alterações de engenharia"),
    CAN_MANAGE_PCP("CanManagePcp", "Acesso ao Pcp"),
    CAN_MANAGE_OP("CanManageOp", "Gerenciamento de OPs");

    private final String code;
    private final String description;

    PermissionsEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PermissionsEnum fromCode(String code) {
        for (PermissionsEnum p : values()) {
            if (p.code.equals(code)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown permission code: " + code);
    }
}
