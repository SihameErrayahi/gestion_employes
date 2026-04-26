DROP DATABASE IF EXISTS gestion_employes;

CREATE DATABASE gestion_employes;
USE gestion_employes;

CREATE TABLE employe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE,
    telephone VARCHAR(20),
    poste VARCHAR(100) NOT NULL,
    departement VARCHAR(100),
    date_embauche DATE NOT NULL,
    salaire_base DECIMAL(10,2) NOT NULL DEFAULT 0,
    cin VARCHAR(20),
    adresse VARCHAR(255),
    statut ENUM('ACTIF','INACTIF','SUSPENDU') NOT NULL DEFAULT 'ACTIF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE conge (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employe_id INT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    nombre_jours INT NOT NULL,
    type ENUM('ANNUEL','MALADIE','MATERNITE','SANS_SOLDE') NOT NULL,
    statut ENUM('EN_ATTENTE','APPROUVE','REFUSE') NOT NULL DEFAULT 'EN_ATTENTE',
    motif VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE
);

CREATE TABLE salaire (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employe_id INT NOT NULL,
    mois VARCHAR(7) NOT NULL,
    salaire_base DECIMAL(10,2) NOT NULL DEFAULT 0,
    primes DECIMAL(10,2) NOT NULL DEFAULT 0,
    retenues DECIMAL(10,2) NOT NULL DEFAULT 0,
    salaire_net DECIMAL(10,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE,
    UNIQUE KEY uq_employe_mois (employe_id, mois)
);

CREATE TABLE utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(80) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role ENUM('RH','RESPONSABLE') NOT NULL DEFAULT 'RH',
    employe_id INT,
    FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE SET NULL
);

INSERT INTO employe (nom, prenom, email, telephone, poste, departement, date_embauche, salaire_base, cin, statut)
VALUES
('Alami', 'Youssef', 'youssef.alami@rh.ma', '0661234567', 'Développeur Java', 'Informatique', '2021-03-15', 12000, 'AB123456', 'ACTIF'),
('Benali', 'Fatima', 'fatima.benali@rh.ma', '0662345678', 'Chef de projet', 'Informatique', '2019-06-01', 16000, 'BC234567', 'ACTIF'),
('Chaoui', 'Mohamed', 'mohamed.chaoui@rh.ma', '0663456789', 'Comptable', 'Finance', '2020-09-10', 11000, 'CD345678', 'ACTIF'),
('Darif', 'Sara', 'sara.darif@rh.ma', '0664567890', 'RH Manager', 'RH', '2018-01-20', 14000, 'DE456789', 'ACTIF'),
('El Fassi', 'Omar', 'omar.elfassi@rh.ma', '0665678901', 'Commercial', 'Commercial', '2022-07-05', 10000, 'EF567890', 'ACTIF');

INSERT INTO utilisateur (login, mot_de_passe, role, employe_id)
VALUES
('admin', 'admin123', 'RESPONSABLE', NULL),
('rh', 'rh123', 'RH', 4);

INSERT INTO conge (employe_id, date_debut, date_fin, nombre_jours, type, statut, motif)
VALUES
(1, '2025-08-01', '2025-08-15', 15, 'ANNUEL', 'EN_ATTENTE', 'Vacances ete'),
(2, '2025-07-10', '2025-07-12', 3, 'MALADIE', 'APPROUVE', 'Grippe'),
(3, '2025-09-01', '2025-09-05', 5, 'ANNUEL', 'EN_ATTENTE', 'Voyage famille');

INSERT INTO salaire (employe_id, mois, salaire_base, primes, retenues, salaire_net)
VALUES
(1, '2025-05', 12000, 1000, 800, 12200),
(2, '2025-05', 16000, 2000, 1500, 16500),
(3, '2025-05', 11000, 500, 700, 10800),
(4, '2025-05', 14000, 1500, 1200, 14300);