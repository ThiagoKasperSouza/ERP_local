CREATE TABLE permissions (
    id VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);

INSERT INTO permissions (id, description) VALUES
('CanUseVault', 'Acesso ao cofre'),
('CanVersion', 'Acesso ao fluxo de versionamento'),
('CanApprove', 'Acesso aos workflows'),
('CanScript', 'Acesso aos roteiros'),
('CanTime', 'Acesso aos tempos e métodos'),
('CanMachine', 'Acesso ao gerenciamento de máquinas'),
('CanInstruct', 'Acesso as intruções'),
('CanRegister', 'Acesso ao cadastro mestre'),
('CanManageMbom', 'Acesso ao gerenciamento de MBOM'),
('CanManageEng', 'Gerenciamento das alterações de engenharia'),
('CanManagePcp', 'Acesso ao Pcp'),
('CanManageOp', 'Gerenciamento de OPs');