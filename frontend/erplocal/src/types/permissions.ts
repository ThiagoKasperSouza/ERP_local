// src/types/permissions.ts

export enum Permission {
    CanUseVault = "CanUseVault",
    CanVersion = "CanVersion",
    CanApprove = "CanApprove",
    CanScript = "CanScript",
    CanTime = "CanTime",
    CanMachine = "CanMachine",
    CanInstruct = "CanInstruct",
    CanRegister = "CanRegister",
    CanManageMbom = "CanManageMbom",
    CanManageEng = "CanManageEng",
    CanManagePcp = "CanManagePcp",
    CanManageOp = "CanManageOp",
}

export const PermissionDescriptions: Record<Permission, string> = {
    [Permission.CanUseVault]: "Acesso ao cofre",
    [Permission.CanVersion]: "Acesso ao fluxo de versionamento",
    [Permission.CanApprove]: "Acesso aos workflows",
    [Permission.CanScript]: "Acesso aos roteiros",
    [Permission.CanTime]: "Acesso aos tempos e métodos",
    [Permission.CanMachine]: "Acesso ao gerenciamento de máquinas",
    [Permission.CanInstruct]: "Acesso as intruções",
    [Permission.CanRegister]: "Acesso ao cadastro mestre",
    [Permission.CanManageMbom]: "Acesso ao gerenciamento de MBOM",
    [Permission.CanManageEng]: "Gerenciamento das alterações de engenharia",
    [Permission.CanManagePcp]: "Acesso ao Pcp",
    [Permission.CanManageOp]: "Gerenciamento de OPs",
};

export function hasPermission(user: Permission[], required: Permission): boolean {
    return user.includes(required);
}
// uso: if(!hasPermission(me.permissions, Permission.CanManagePcp)) redirect('/login')