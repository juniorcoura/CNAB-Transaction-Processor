DROP DATABASE  IF EXISTS `transacoes`;
CREATE DATABASE `transacoes`;
USE `transacoes`;

CREATE TABLE `transacoes`(
	`id` INT NOT NULL AUTO_INCREMENT,
    `tipo` INT DEFAULT NULL,
    `data` DATE DEFAULT NULL,
    `valor` DECIMAL DEFAULT NULL,
    `cpf` VARCHAR(255) DEFAULT NULL,
    `cartao` VARCHAR(255) DEFAULT NULL,
    `hora` TIME DEFAULT NULL,
    `dono_loja` VARCHAR(255) DEFAULT NULL,
    `nome_loja` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY(`id`)
    ) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
  
    