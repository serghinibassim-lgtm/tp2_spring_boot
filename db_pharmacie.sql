use db_spring_secur;

INSERT INTO categorie (id, nom, description)
VALUES (1, 'Antibiotiques', 'Médicaments utilisés pour traiter les infectionsbactériennes.'),
       (2, 'Antalgiques', 'Médicaments pour soulager la douleur.'),
       (3, 'Vitamines', 'Compléments nutritionnels et fortifiants.'),
       (4, 'Antiseptiques', 'Produits désinfectants pour usage externe.');



select *
from categorie;

INSERT INTO fournisseure (id, nom, contact, adresse)
VALUES (1, 'PharmaMaroc', 'contact@pharmamaroc.ma', 'Casablanca, Maroc'),
       (2, 'Medisup France', 'info@medisup.fr', 'Paris, France'),
       (3, 'Sandoz', 'export@sandoz.com', 'Bâle, Suisse'),
       (4, 'Cooper Pharma', 'support@cooperpharma.ma', 'Rabat, Maroc');


INSERT INTO detail_produit (id, fabricant, pays_origine, composition, dosage,
                            indication)
VALUES (1, 'Sanofi', 'France', 'Amoxicilline trihydratée 500 mg', '500 mg', 'Infections
ORL et respiratoires'),
       (2, 'Bayer', 'Allemagne', 'Paracétamol 500 mg', '500 mg', 'Douleurs légères à
modérées, fièvre'),
       (3, 'Pfizer', 'États-Unis', 'Vitamine C 1000 mg', '1000 mg', 'Renforcement
immunitaire'),
       (4, 'Johnson & Johnson', 'Maroc', 'Povidone iodée 10%', '10%', 'Désinfection des
plaies'),
       (5, 'GSK', 'Royaume-Uni', 'Ibuprofène 400 mg', '400 mg', 'Inflammations et
douleurs musculaires');


INSERT INTO tag (id, libelle)
VALUES (1, 'Sans ordonnance'),
       (2, 'Prescription obligatoire'),
       (3, 'En promotion'),
       (4, 'Stock limité'),
       (5, 'Importé');


INSERT INTO produit (id, nom, description, prix, code_bare, date_expiration,
                     categorie_id, fourniseur_id, detail_produit_id)
VALUES (1, 'Amoxicilline 500mg', 'Antibiotique à large spectre.', 45.00, 'AMX500-2025',
        '2026-03-10', 1, 1, 1),
       (2, 'Doliprane 500mg', 'Antalgique et antipyrétique.', 25.50, 'DLP500-2025',
        '2025-11-01', 2, 2, 2),
       (3, 'Vitamine C 1000mg', 'Complément pour renforcer les défenses.', 30.00,
        'VITC1000-2025', '2026-06-15', 3, 3, 3),
       (4, 'Bétadine Solution 10%', 'Désinfectant pour usage externe.', 18.00,
        'BETADINE10-2025', '2027-01-30', 4, 4, 4),
       (5, 'Nurofen 400mg', 'Soulage la douleur et la fièvre.', 42.00, 'NURO400-2025',
        '2026-02-20', 2, 3, 5);


INSERT INTO produit_tag (produit_id, tag_id) VALUES
                                                 (1, 2), -- Amoxicilline : prescription obligatoire
                                                 (2, 1), -- Doliprane : sans ordonnance
                                                 (2, 3), -- Doliprane : en promotion
                                                 (3, 1), -- Vitamine C : sans ordonnance
                                                 (3, 5), -- Vitamine C : importé
                                                 (4, 1), -- Bétadine : sans ordonnance
                                                 (4, 4), -- Bétadine : stock limité
                                                 (5, 1), -- Nurofen : sans ordonnance
                                                 (5, 5); -- Nurofen : importé

