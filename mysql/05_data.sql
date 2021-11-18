--
-- Data
--

INSERT INTO CMS_LINGUE(ID_LINGUA,CODICE,DESCRIZIONE,ATTIVO) VALUES (0, 'IT', 'Italiano', 'S'); 
INSERT INTO CMS_LINGUE(ID_LINGUA,CODICE,DESCRIZIONE,ATTIVO) VALUES (1, 'EN', 'English',  'S');
INSERT INTO CMS_LINGUE(ID_LINGUA,CODICE,DESCRIZIONE,ATTIVO) VALUES (2, 'FR', 'Francais', 'S');
INSERT INTO CMS_LINGUE(ID_LINGUA,CODICE,DESCRIZIONE,ATTIVO) VALUES (3, 'DE', 'Deutsche', 'S');
INSERT INTO CMS_LINGUE(ID_LINGUA,CODICE,DESCRIZIONE,ATTIVO) VALUES (4, 'ES', 'Espanol',  'S');
COMMIT;

INSERT INTO CMS_TIPI_CONTENUTO(ID_TIPO_CONTENUTO,DESCRIZIONE,ATTIVO) VALUES (0, '-',         'N');
INSERT INTO CMS_TIPI_CONTENUTO(ID_TIPO_CONTENUTO,DESCRIZIONE,ATTIVO) VALUES (1, 'Video',     'S'); 
INSERT INTO CMS_TIPI_CONTENUTO(ID_TIPO_CONTENUTO,DESCRIZIONE,ATTIVO) VALUES (2, 'Immagine',  'S'); 
INSERT INTO CMS_TIPI_CONTENUTO(ID_TIPO_CONTENUTO,DESCRIZIONE,ATTIVO) VALUES (3, 'Audio',     'S'); 
INSERT INTO CMS_TIPI_CONTENUTO(ID_TIPO_CONTENUTO,DESCRIZIONE,ATTIVO) VALUES (4, 'Documento', 'S');
COMMIT;

INSERT INTO CMS_TIPI_UTENTE(ID_TIPO_UTENTE,DESCRIZIONE,ATTIVO) VALUES (0, 'Esterno', 'S'); 
INSERT INTO CMS_TIPI_UTENTE(ID_TIPO_UTENTE,DESCRIZIONE,ATTIVO) VALUES (1, 'Interno', 'S');
COMMIT;

INSERT INTO CMS_TIPI_AUTORE(ID_TIPO_AUTORE,DESCRIZIONE,ATTIVO) VALUES (0, '-', 'N'); 
COMMIT;

INSERT INTO CMS_AUTORI(ID_AUTORE,ID_TIPO_AUTORE,COGNOME,NOME) VALUES (0, 0, '-', '-'); 
COMMIT;

INSERT INTO CMS_TIPI_LUOGO(ID_TIPO_LUOGO,CODICE,ATTIVO) VALUES (0, '-', 'N');
COMMIT;

INSERT INTO CMS_COMUNI(ID_COMUNE,COD_ISTAT,COD_FISCALE,DESCRIZIONE,PROVINCIA) VALUES(0, '000000', 'A000', '-', NULL);
COMMIT;

INSERT INTO CMS_LUOGHI(ID_LUOGO,ID_TIPO_LUOGO,CODICE,DESCRIZIONE,ID_COMUNE,ATTIVO) VALUES (0, 0, '-', '-', 0, 'N');
COMMIT;

INSERT INTO CMS_CATEGORIE(ID_CATEGORIA,CODICE,ATTIVO) VALUES(0, '-', 'N');
COMMIT;

INSERT INTO CMS_SOTTOCATEGORIE(ID_SOTTOCATEGORIA,ID_CATEGORIA,CODICE,ATTIVO) VALUES(0, 0, '-', 'N');
COMMIT;

INSERT INTO CMS_TIPI_ARTICOLO(ID_TIPO_ARTICOLO,CODICE,ATTIVO) VALUES(0, '-', 'N');
COMMIT;

INSERT INTO CMS_ISTITUTI(ID_ISTITUTO,CODICE,DESCRIZIONE,ATTIVO) VALUES(0, '-', '-', 'N');
COMMIT;

INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (0,  '-',                  'N');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (1,  'Pagina pubblica',    'S');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (2,  'Pagina privata',     'S');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (3,  'Menu intestazione',  'S');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (4,  'Menu navigazione',   'S');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (5,  'Menu footer',        'S');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (6,  'Citazione',          'S');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (7,  'Elenco singolo',     'S');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (8,  'Elenco multiplo',    'S');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (9,  'Elenco principale',  'S');
INSERT INTO CMS_TIPI_PAGINA(ID_TIPO_PAGINA,DESCRIZIONE,ATTIVO) VALUES (10, 'Elenco in evidenza', 'S');
COMMIT;

INSERT INTO CMS_PAGINE(ID_PAGINA,ID_TIPO_PAGINA,CODICE,ID_CATEGORIA,ID_SOTTOCATEGORIA,ID_TIPO_ARTICOLO,ORDINE,ATTIVO) VALUES(1,1,'HOME',0,0,0,0,'S');
INSERT INTO CMS_PAGINE(ID_PAGINA,ID_TIPO_PAGINA,CODICE,ID_CATEGORIA,ID_SOTTOCATEGORIA,ID_TIPO_ARTICOLO,ORDINE,ATTIVO) VALUES(2,1,'VIEW',0,0,0,0,'S');
COMMIT;

INSERT INTO CMS_PAGINE_DESC(ID_PAGINA,ID_LINGUA,DESCRIZIONE) VALUES (1, 0,'Home');
INSERT INTO CMS_PAGINE_DESC(ID_PAGINA,ID_LINGUA,DESCRIZIONE) VALUES (2, 0,'Dettaglio articolo');
INSERT INTO CMS_PAGINE_DESC(ID_PAGINA,ID_LINGUA,DESCRIZIONE) VALUES (1, 1,'Home');
INSERT INTO CMS_PAGINE_DESC(ID_PAGINA,ID_LINGUA,DESCRIZIONE) VALUES (2, 1,'View article');
COMMIT;

INSERT INTO CMS_UTENTI(ID_UTENTE,USERNAME,PASSWORD,ID_TIPO_UTENTE,COGNOME,NOME,SESSO,DATA_NASCITA,PROFESSIONE,CITTA,EMAIL,ATTIVO,DT_INS) VALUES (1,'admin','h(3u1',1,'ADMIN','ADMIN','M',NULL,NULL,NULL,NULL,'S',SYSDATE());
COMMIT;

UPDATE CMS_PROGRESSIVI SET VALORE=4  WHERE CODICE='SEQ_CMS_LINGUE';
UPDATE CMS_PROGRESSIVI SET VALORE=4  WHERE CODICE='SEQ_CMS_TIPI_CONTENUTO';
UPDATE CMS_PROGRESSIVI SET VALORE=1  WHERE CODICE='SEQ_CMS_TIPI_UTENTE';
UPDATE CMS_PROGRESSIVI SET VALORE=10 WHERE CODICE='SEQ_CMS_TIPI_PAGINA';
UPDATE CMS_PROGRESSIVI SET VALORE=3  WHERE CODICE='SEQ_CMS_PAGINE';
COMMIT;
