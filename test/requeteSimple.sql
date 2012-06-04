SELECT
 `avis`.`idAvis` as identifiant_avis
,`avis`.`AVI_idClient` as identifiant_client
,`avis`.`AVI_DateEmission` as date_avis
,`avis`.`AVI_Montant`/100 as montant_total
,`avis`.`AVI_DateLimitePaiement` as date_paiement
,`client`.`CLI_Intitule` as client
FROM avis
LEFT JOIN 
  `base_test_paiement_creancier`.`client`
    ON `avis`.`AVI_idClient` = `client`.`idClient` 
WHERE
( AVI_Statut = 'validé')
AND
( AVI_DateEmission >= #SMURF#DateTimeRequestBegin)
AND
( AVI_DateEmission < #SMURF#DateTimeRequestEnd)

