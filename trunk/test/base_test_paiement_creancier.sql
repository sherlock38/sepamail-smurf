-- Génération MSO pour IDSoft 
-- 20120224


SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de données: `base_test_paiement_creancier`
--

-- --------------------------------------------------------

--
-- Structure de la table `adresse`
--

CREATE TABLE IF NOT EXISTS `adresse` (
  `idAdresse` int(11) NOT NULL COMMENT 'identifiant de l''adresse',
  `ADR_Type` enum('facturation et correspondance','facturation','correspondance','autre') NOT NULL COMMENT 'type de l''adresse',
  `ADR_Ligne1` varchar(100) NOT NULL COMMENT 'ligne1 de l''adresse',
  `ADR_Ligne2` varchar(100) NOT NULL COMMENT 'ligne2 de l''adresse',
  `ADR_Ligne3` varchar(100) NOT NULL COMMENT 'ligne3 de l''adresse',
  `ADR_CodeLocalite` varchar(10) NOT NULL COMMENT 'code de la localité',
  `ADR_Localite` varchar(100) NOT NULL COMMENT 'intitulé de la localité',
  `ADR_CodePays` varchar(2) NOT NULL COMMENT 'code du pays',
  `ADR_Statut` enum('actif','inactif') NOT NULL COMMENT 'statut de l''adresse',
  `ADR_idClient` int(11) NOT NULL COMMENT 'identifiant du client lié à l''adresse',
  `ADR_DateCreation` datetime NOT NULL COMMENT 'date de création de la fiche adresse',
  `ADR_DateModification` datetime NOT NULL COMMENT 'date de modification de la fiche adresse',
  PRIMARY KEY (`idAdresse`),
  KEY `ADR_idClient` (`ADR_idClient`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='les adresses de facturation';

--
-- RELATIONS POUR LA TABLE `adresse`:
--   `ADR_idClient`
--       `client` -> `idClient`
--

--
-- Contenu de la table `adresse`
--

INSERT INTO `adresse` (`idAdresse`, `ADR_Type`, `ADR_Ligne1`, `ADR_Ligne2`, `ADR_Ligne3`, `ADR_CodeLocalite`, `ADR_Localite`, `ADR_CodePays`, `ADR_Statut`, `ADR_idClient`, `ADR_DateCreation`, `ADR_DateModification`) VALUES
(1497781831, 'facturation et correspondance', '5, place Lapérouse', '', '', '81000', 'ALBI', 'FR', 'actif', 128248723, '2009-05-14 10:05:52', '2009-05-14 10:05:52'),
(59784301, 'facturation', '22, avenue Jean Gagnant', '', '', '87000', 'LIMOGES', 'FR', 'actif', 824446841, '2005-09-02 11:53:12', '2005-09-02 11:53:12'),
(17984517, 'correspondance', '1et 3, Rue du Moulin', '', '', '87000', 'LIMOGES', 'FR', 'actif', 824446841, '2005-09-02 11:58:42', '2005-09-02 11:58:42'),
(2147483647, 'facturation et correspondance', '50 avenue de Bretagne', 'Batiment Q', '', '76000', 'ROUEN', 'FR', 'actif', 911649815, '2010-03-31 09:05:45', '2010-03-31 09:05:45'),
(39874545, 'facturation et correspondance', '50 avenue de Bretagne', 'Batiment Q', '', '76000', 'ROUEN', 'FR', 'actif', 911649815, '2010-03-31 09:05:45', '2010-03-31 09:05:45'),
(222121078, 'facturation et correspondance', '189 QUAI GALLIENI', 'Appartement 132', 'Résidence du clos', '94500', 'CHAMPIGNY', 'FR', 'actif', 1052049806, '2010-01-02 13:58:14', '2010-01-02 13:58:14');

-- --------------------------------------------------------

--
-- Structure de la table `avis`
--

CREATE TABLE IF NOT EXISTS `avis` (
  `idAvis` int(11) NOT NULL COMMENT 'identifiant de l''avis',
  `AVI_idClient` int(11) NOT NULL COMMENT 'identifiant du client lié à l''avis',
  `AVI_DateEmission` date NOT NULL COMMENT 'date de l''émission de l''avis',
  `AVI_Montant` int(11) NOT NULL COMMENT 'montant en centimes de l''avis',
  `AVI_DateLimitePaiement` date NOT NULL COMMENT 'Date limite de paiement',
  `AVI_Statut` enum('à_vérifier','validé','envoyé','payé','annulé') NOT NULL COMMENT 'statut de l''avis',
  PRIMARY KEY (`idAvis`),
  KEY `AVI_idClient` (`AVI_idClient`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='les avis de paiements';

--
-- RELATIONS POUR LA TABLE `avis`:
--   `AVI_idClient`
--       `client` -> `idClient`
--

--
-- Contenu de la table `avis`
--

INSERT INTO `avis` (`idAvis`, `AVI_idClient`, `AVI_DateEmission`, `AVI_Montant`, `AVI_DateLimitePaiement`, `AVI_Statut`) VALUES
(1271033454, 128248723, '2012-02-01', 1234567, '2012-03-15', 'validé'),
(818570891, 824446841, '2012-02-01', 97815, '2012-03-15', 'validé'),
(640696853, 911649815, '2012-02-14', 45645, '2012-03-20', 'validé'),
(78154241, 1052049806, '2012-02-15', 69781, '2012-03-31', 'validé');

-- --------------------------------------------------------

--
-- Structure de la table `avis_detail`
--

CREATE TABLE IF NOT EXISTS `avis_detail` (
  `IDAvisDetail` int(11) NOT NULL COMMENT 'identifiant de la ligne de l''avis',
  `AVD_idAvis` int(11) NOT NULL COMMENT 'identifiant de l''avis lié à la ligne',
  `AVD_Intitule` varchar(200) NOT NULL COMMENT 'Intitulé de la ligne de l''avis de paiement',
  `AVD_Montant` int(11) NOT NULL COMMENT 'montant de la ligne',
  `AVD_Reference` varchar(20) NOT NULL COMMENT 'Référence de la ligne',
  `AVD_Ordre` int(11) NOT NULL COMMENT 'Ordre de la ligne',
  PRIMARY KEY (`IDAvisDetail`),
  UNIQUE KEY `AVD_idAvis_Ordre` (`AVD_idAvis`,`AVD_Ordre`),
  KEY `AVD_idAvis` (`AVD_idAvis`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Les lignes détail de l''avis';

--
-- RELATIONS POUR LA TABLE `avis_detail`:
--   `AVD_idAvis`
--       `avis` -> `idAvis`
--

--
-- Contenu de la table `avis_detail`
--

INSERT INTO `avis_detail` (`IDAvisDetail`, `AVD_idAvis`, `AVD_Intitule`, `AVD_Montant`, `AVD_Reference`, `AVD_Ordre`) VALUES
(123456789, 78154241, 'Loyer de Février', 56104, 'LOY201202', 1),
(987654321, 78154241, 'Charges de Janvier', 13677, 'CHG201001', 2),
(14578412, 640696853, 'Loyer de Février 2012', 34243, 'LOY201202', 1),
(6754180, 640696853, 'Charges de Janvier 2012', 11411, 'CHG201201', 2),
(6574819, 818570891, 'Loyer de Février 2012', 35599, 'LOY201202', 1),
(9481571, 818570891, 'Charges de Janvier 2012', 15201, 'CHG201201', 2),
(94781345, 818570891, 'Reste du Solde Client', 47015, 'SOL201201', 3),
(97815420, 1271033454, 'Loyer Trimestre 1 année 2012', 1234567, '2012T1', 1);

-- --------------------------------------------------------

--
-- Structure de la table `client`
--

CREATE TABLE IF NOT EXISTS `client` (
  `idClient` int(11) NOT NULL COMMENT 'identifiant du client',
  `CLI_Intitule` varchar(100) NOT NULL COMMENT 'intitulé du client',
  `CLI_Statut` enum('actif','inactif') NOT NULL COMMENT 'statut du client',
  `CLI_DateCreation` datetime NOT NULL COMMENT 'Date de création de la fiche client',
  `CLI_DateModification` datetime NOT NULL COMMENT 'Date de modification de la fiche client',
  PRIMARY KEY (`idClient`),
  UNIQUE KEY `CLI_Intitule` (`CLI_Intitule`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='clients';

--
-- Contenu de la table `client`
--

INSERT INTO `client` (`idClient`, `CLI_Intitule`, `CLI_Statut`, `CLI_DateCreation`, `CLI_DateModification`) VALUES
(128248723, 'Famille LEFEVRE', 'actif', '2009-05-14 10:05:52', '2012-02-01 17:05:20'),
(824446841, 'Ameline DUPONT', 'actif', '2005-09-02 11:53:12', '2005-09-02 11:53:12'),
(911649815, 'Association MLF', 'inactif', '2003-12-24 11:15:00', '2010-03-31 09:05:45'),
(1052049806, 'M. Jean DUPORT', 'actif', '2010-01-02 13:58:14', '2010-01-02 13:58:14');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
