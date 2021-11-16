-- Tables

CREATE TABLE CMS_LINGUE (
   ID_LINGUA          NUMBER(10)      NOT NULL,
   CODICE             VARCHAR2(10)    NOT NULL,
   DESCRIZIONE        VARCHAR2(50)    NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_LINGUE PRIMARY KEY (ID_LINGUA) );

CREATE TABLE CMS_TAG (
   ID_TAG             NUMBER(10)      NOT NULL,
   CODICE             VARCHAR2(50)    NOT NULL,
   ANTEPRIMA          VARCHAR2(1)     DEFAULT 'N',
   ORDINE             NUMBER(10)      DEFAULT 0,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_TAG PRIMARY KEY (ID_TAG) );

CREATE TABLE CMS_TAG_DESC (
   ID_TAG             NUMBER(10)      NOT NULL,
   ID_LINGUA          NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   CONSTRAINT PK_CMS_TAG_DESC PRIMARY KEY (ID_TAG, ID_LINGUA) );

CREATE TABLE CMS_ISTITUTI (
   ID_ISTITUTO        NUMBER(10)      NOT NULL,
   CODICE             VARCHAR2(50)    NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_ISTITUTI PRIMARY KEY (ID_ISTITUTO) );

CREATE TABLE CMS_COMUNI (
   ID_COMUNE          NUMBER(10)      NOT NULL,
   COD_ISTAT          VARCHAR2(6)     NOT NULL,
   COD_FISCALE        VARCHAR2(4)     NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   PROVINCIA          VARCHAR2(4),
   CONSTRAINT PK_CMS_COMUNI PRIMARY KEY (ID_COMUNE) );

CREATE TABLE CMS_TIPI_LUOGO (
   ID_TIPO_LUOGO      NUMBER(10)      NOT NULL,
   CODICE             VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_TIPI_LUOGO PRIMARY KEY (ID_TIPO_LUOGO) );
   
CREATE TABLE CMS_TIPI_LUOGO_DESC (
   ID_TIPO_LUOGO      NUMBER(10)      NOT NULL,
   ID_LINGUA          NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255),
   CONSTRAINT PK_CMS_TIPI_LUOGO_DESC PRIMARY KEY (ID_TIPO_LUOGO, ID_LINGUA),
   FOREIGN KEY (ID_LINGUA) REFERENCES CMS_LINGUE (ID_LINGUA) );   
   
CREATE TABLE CMS_LUOGHI (
   ID_LUOGO           NUMBER(10)      NOT NULL,
   ID_TIPO_LUOGO      NUMBER(10)      NOT NULL,
   CODICE             VARCHAR2(50)    NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   ID_COMUNE          NUMBER(10)      NOT NULL,
   INDIRIZZO          VARCHAR2(255),
   CAP                VARCHAR2(5),
   SITO_WEB           VARCHAR2(255),
   EMAIL              VARCHAR2(255),
   TEL_1              VARCHAR2(50),
   TEL_2              VARCHAR2(50),
   FAX                VARCHAR2(50),
   INFORMAZIONI       VARCHAR2(100),
   LATITUDINE         NUMBER(15,12),
   LONGITUDINE        NUMBER(15,12),
   RICERCA            VARCHAR2(255),
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_LUOGHI PRIMARY KEY (ID_LUOGO),
   FOREIGN KEY (ID_TIPO_LUOGO) REFERENCES CMS_TIPI_LUOGO (ID_TIPO_LUOGO),
   FOREIGN KEY (ID_COMUNE) REFERENCES CMS_COMUNI (ID_COMUNE));

CREATE TABLE CMS_TIPI_AUTORE (
   ID_TIPO_AUTORE     NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_TIPI_AUTORE PRIMARY KEY (ID_TIPO_AUTORE) );

CREATE TABLE CMS_TIPI_CONTENUTO (
   ID_TIPO_CONTENUTO  NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_TIPI_CONTENUTO PRIMARY KEY (ID_TIPO_CONTENUTO) );

CREATE TABLE CMS_TIPI_UTENTE (
   ID_TIPO_UTENTE     NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_TIPI_UTENTE PRIMARY KEY (ID_TIPO_UTENTE) );

CREATE TABLE CMS_AUTORI (
   ID_AUTORE          NUMBER(10)      NOT NULL,
   ID_TIPO_AUTORE     NUMBER(10)      NOT NULL,
   COGNOME            VARCHAR2(50)    NOT NULL,
   NOME               VARCHAR2(50)    NOT NULL,
   SESSO              VARCHAR2(1),
   DATA_NASCITA       DATE,
   TITOLO             VARCHAR2(50),
   TELEFONO           VARCHAR2(50),
   CELLULARE          VARCHAR2(50),
   EMAIL              VARCHAR2(100),
   NOTE               VARCHAR2(4000),
   CONSTRAINT PK_CMS_AUTORI PRIMARY KEY (ID_AUTORE),
   FOREIGN KEY (ID_TIPO_AUTORE) REFERENCES CMS_TIPI_AUTORE (ID_TIPO_AUTORE) );

CREATE TABLE CMS_UTENTI (
   ID_UTENTE          NUMBER(10)      NOT NULL,
   USERNAME           VARCHAR2(50)    NOT NULL,
   PASSWORD           VARCHAR2(50)    NOT NULL,
   ID_TIPO_UTENTE     NUMBER(10)      NOT NULL,
   COGNOME            VARCHAR2(50),
   NOME               VARCHAR2(50),
   SESSO              VARCHAR2(1),
   DATA_NASCITA       DATE,
   PROFESSIONE        VARCHAR2(50),
   CITTA              VARCHAR2(50),
   EMAIL              VARCHAR2(100),
   ATTIVO             VARCHAR2(1)     NOT NULL,
   DT_INS             TIMESTAMP       NOT NULL,
   CONSTRAINT PK_CMS_UTENTI PRIMARY KEY (ID_UTENTE),
   FOREIGN KEY (ID_TIPO_UTENTE) REFERENCES CMS_TIPI_UTENTE (ID_TIPO_UTENTE) );

CREATE TABLE CMS_RUOLI (
   ID_RUOLO           NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_RUOLI PRIMARY KEY (ID_RUOLO) );

CREATE TABLE CMS_CATEGORIE (
   ID_CATEGORIA       NUMBER(10)      NOT NULL,
   CODICE             VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_CATEGORIE PRIMARY KEY (ID_CATEGORIA) );   

CREATE TABLE CMS_CATEGORIE_DESC (
   ID_CATEGORIA       NUMBER(10)      NOT NULL,
   ID_LINGUA          NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255),
   CONSTRAINT PK_CMS_CATEGORIE_DESC PRIMARY KEY (ID_CATEGORIA, ID_LINGUA),
   FOREIGN KEY (ID_LINGUA) REFERENCES CMS_LINGUE (ID_LINGUA) );   
   
CREATE TABLE CMS_SOTTOCATEGORIE (
   ID_SOTTOCATEGORIA  NUMBER(10)      NOT NULL,
   ID_CATEGORIA       NUMBER(10)      NOT NULL,
   CODICE             VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_SOTTOCATEGORIE PRIMARY KEY (ID_SOTTOCATEGORIA),
   FOREIGN KEY (ID_CATEGORIA) REFERENCES CMS_CATEGORIE (ID_CATEGORIA) );   

CREATE TABLE CMS_SOTTOCATEGORIE_DESC (
   ID_SOTTOCATEGORIA  NUMBER(10)      NOT NULL,
   ID_LINGUA          NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255),
   CONSTRAINT PK_CMS_SOTTOCATEGORIE_DESC PRIMARY KEY (ID_SOTTOCATEGORIA, ID_LINGUA),
   FOREIGN KEY (ID_LINGUA) REFERENCES CMS_LINGUE (ID_LINGUA) );

CREATE TABLE CMS_TIPI_ARTICOLO (
   ID_TIPO_ARTICOLO   NUMBER(10)      NOT NULL,
   CODICE             VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_TIPI_ARTICOLO PRIMARY KEY (ID_TIPO_ARTICOLO) );   

CREATE TABLE CMS_TIPI_ARTICOLO_DESC (
   ID_TIPO_ARTICOLO   NUMBER(10)      NOT NULL,
   ID_LINGUA          NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255),
   CONSTRAINT PK_CMS_TIPI_ARTICOLO_DESC PRIMARY KEY (ID_TIPO_ARTICOLO, ID_LINGUA),
   FOREIGN KEY (ID_LINGUA) REFERENCES CMS_LINGUE (ID_LINGUA) );   
   
CREATE TABLE CMS_ARTICOLI (
   ID_ARTICOLO        NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   DATA_ARTICOLO      DATE            NOT NULL,
   ID_CATEGORIA       NUMBER(10)      NOT NULL,
   ID_SOTTOCATEGORIA  NUMBER(10)      NOT NULL,
   ID_TIPO_ARTICOLO   NUMBER(10)      NOT NULL,
   ID_ISTITUTO        NUMBER(10)      NOT NULL,
   ID_LUOGO           NUMBER(10)      NOT NULL,
   ID_TIPO_UTENTE     NUMBER(10)      NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   DT_INS             TIMESTAMP       NOT NULL,
   UTE_INS            VARCHAR2(50)    NOT NULL,
   DT_AGG             TIMESTAMP,
   UTE_AGG            VARCHAR2(50),
   PREF_POS           NUMBER(10)      DEFAULT 0,
   DT_POS             TIMESTAMP,
   PREF_NEG           NUMBER(10)      DEFAULT 0,
   DT_NEG             TIMESTAMP,
   CONSTRAINT PK_CMS_ARTICOLI PRIMARY KEY (ID_ARTICOLO),
   FOREIGN KEY (ID_CATEGORIA) REFERENCES CMS_CATEGORIE (ID_CATEGORIA),
   FOREIGN KEY (ID_SOTTOCATEGORIA) REFERENCES CMS_SOTTOCATEGORIE (ID_SOTTOCATEGORIA),
   FOREIGN KEY (ID_TIPO_ARTICOLO) REFERENCES CMS_TIPI_ARTICOLO (ID_TIPO_ARTICOLO),
   FOREIGN KEY (ID_ISTITUTO) REFERENCES CMS_ISTITUTI (ID_ISTITUTO),
   FOREIGN KEY (ID_LUOGO) REFERENCES CMS_LUOGHI (ID_LUOGO),
   FOREIGN KEY (ID_TIPO_UTENTE) REFERENCES CMS_TIPI_UTENTE (ID_TIPO_UTENTE) );

CREATE TABLE CMS_ARTICOLI_AUT (
   ID_ARTICOLO        NUMBER(10)      NOT NULL,
   ID_AUTORE          NUMBER(10)      NOT NULL,
   ID_RUOLO           NUMBER(10)      NOT NULL,
   CONSTRAINT PK_CMS_ARTICOLI_AUT PRIMARY KEY (ID_ARTICOLO, ID_AUTORE),
   FOREIGN KEY (ID_AUTORE) REFERENCES CMS_AUTORI (ID_AUTORE),
   FOREIGN KEY (ID_RUOLO) REFERENCES CMS_RUOLI (ID_RUOLO) );

CREATE TABLE CMS_ARTICOLI_LUOGHI (
   ID_ARTICOLO        NUMBER(10)      NOT NULL,
   ID_LUOGO           NUMBER(10)      NOT NULL,
   ORDINE             NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255),
   CONSTRAINT PK_CMS_ARTICOLI_LUOGHI PRIMARY KEY (ID_ARTICOLO, ID_LUOGO),
   FOREIGN KEY (ID_LUOGO) REFERENCES CMS_LUOGHI (ID_LUOGO) );

CREATE TABLE CMS_ARTICOLI_CONT (
   ID_ARTICOLO        NUMBER(10)    NOT NULL,
   ID_LINGUA          NUMBER(10)    NOT NULL,
   TITOLO             VARCHAR2(255  CHAR),
   SPECIFICA          VARCHAR2(255  CHAR),
   ABSTRACT           VARCHAR2(4000 CHAR),
   TESTO              VARCHAR2(4000 CHAR),
   TESTO2             VARCHAR2(4000 CHAR),
   TESTO3             VARCHAR2(4000 CHAR),
   NOTE               VARCHAR2(4000 CHAR),
   RIFERIMENTI        VARCHAR2(4000 CHAR),
   KEYWORDS           VARCHAR2(4000 CHAR),
   CONSTRAINT PK_CMS_ARTICOLI_CONT PRIMARY KEY (ID_ARTICOLO, ID_LINGUA),
   FOREIGN KEY (ID_LINGUA)   REFERENCES CMS_LINGUE   (ID_LINGUA),
   FOREIGN KEY (ID_ARTICOLO) REFERENCES CMS_ARTICOLI (ID_ARTICOLO));
   
CREATE TABLE CMS_ARTICOLI_MULT (
   ID_MULTIMEDIA      NUMBER(10)      NOT NULL,
   ID_LINGUA          NUMBER(10)      NOT NULL,
   ID_TIPO_CONTENUTO  NUMBER(10)      NOT NULL,
   URL_FILE           VARCHAR2(255)   NOT NULL,
   ID_ARTICOLO        NUMBER(10)      NOT NULL,
   ORDINE             NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(4000),
   CONSTRAINT PK_CMS_ARTICOLI_MULT PRIMARY KEY (ID_MULTIMEDIA),
   FOREIGN KEY (ID_LINGUA)   REFERENCES CMS_LINGUE (ID_LINGUA),
   FOREIGN KEY (ID_TIPO_CONTENUTO) REFERENCES CMS_TIPI_CONTENUTO (ID_TIPO_CONTENUTO),
   FOREIGN KEY (ID_ARTICOLO) REFERENCES CMS_ARTICOLI (ID_ARTICOLO));

CREATE TABLE CMS_ARTICOLI_TAG (
   ID_ARTICOLO        NUMBER(10)      NOT NULL,
   ID_TAG             NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255),
   CONSTRAINT PK_CMS_ARTICOLI_TAG PRIMARY KEY (ID_ARTICOLO, ID_TAG),
   FOREIGN KEY (ID_ARTICOLO) REFERENCES CMS_ARTICOLI (ID_ARTICOLO),
   FOREIGN KEY (ID_TAG)      REFERENCES CMS_TAG      (ID_TAG) );
   
CREATE TABLE CMS_ARTICOLI_CORR (
   ID_ARTICOLO        NUMBER(10)      NOT NULL,
   ID_ARTICOLO_CORR   NUMBER(10)      NOT NULL,
   ORDINE             NUMBER(10)      NOT NULL,
   CONSTRAINT PK_CMS_ARTICOLI_CORR PRIMARY KEY (ID_ARTICOLO, ID_ARTICOLO_CORR),
   FOREIGN KEY (ID_ARTICOLO)      REFERENCES CMS_ARTICOLI (ID_ARTICOLO),
   FOREIGN KEY (ID_ARTICOLO_CORR) REFERENCES CMS_ARTICOLI (ID_ARTICOLO) );

CREATE TABLE CMS_ARTICOLI_COMP (
   ID_ARTICOLO        NUMBER(10)      NOT NULL,
   ID_ARTICOLO_COMP   NUMBER(10)      NOT NULL,
   ORDINE             NUMBER(10)      NOT NULL,
   CONSTRAINT PK_CMS_ARTICOLI_COMP PRIMARY KEY (ID_ARTICOLO, ID_ARTICOLO_COMP),
   FOREIGN KEY (ID_ARTICOLO)      REFERENCES CMS_ARTICOLI (ID_ARTICOLO),
   FOREIGN KEY (ID_ARTICOLO_COMP) REFERENCES CMS_ARTICOLI (ID_ARTICOLO) );

CREATE TABLE CMS_ARTICOLI_PREZZI (
   ID_ARTICOLO        NUMBER(10)    NOT NULL,
   ID_LINGUA          NUMBER(10)    NOT NULL,
   ORDINE             NUMBER(10)    NOT NULL,
   CODICE             VARCHAR2(50),
   DESCRIZIONE        VARCHAR2(255),
   PREZZO             NUMBER(8,2), 
   SCONTO             NUMBER(3),
   SCONTATO           NUMBER(8,2),
   ACCONTO            NUMBER(8,2),
   PROMOZIONE         VARCHAR2(1),
   CONSTRAINT PK_CMS_ARTICOLI_PREZZI PRIMARY KEY (ID_ARTICOLO, ID_LINGUA, ORDINE),
   FOREIGN KEY (ID_LINGUA)   REFERENCES CMS_LINGUE   (ID_LINGUA),
   FOREIGN KEY (ID_ARTICOLO) REFERENCES CMS_ARTICOLI (ID_ARTICOLO));

CREATE TABLE CMS_TIPI_PAGINA (
   ID_TIPO_PAGINA     NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255)   NOT NULL,
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_TIPI_PAGINA PRIMARY KEY (ID_TIPO_PAGINA) );

CREATE TABLE CMS_PAGINE (
   ID_PAGINA          NUMBER(10)      NOT NULL,
   ID_TIPO_PAGINA     NUMBER(10)      NOT NULL,
   CODICE             VARCHAR2(50)    NOT NULL,
   ID_CATEGORIA       NUMBER(10)      NOT NULL,
   ID_SOTTOCATEGORIA  NUMBER(10)      NOT NULL,
   ID_TIPO_ARTICOLO   NUMBER(10)      NOT NULL,
   ORDINE             NUMBER(10)      NOT NULL,
   RIGHE              NUMBER(10),
   COLONNE            NUMBER(10),
   VISTA              NUMBER(10),
   ATTIVO             VARCHAR2(1)     NOT NULL,
   CONSTRAINT PK_CMS_PAGINE PRIMARY KEY (ID_PAGINA),
   FOREIGN KEY (ID_CATEGORIA)      REFERENCES CMS_CATEGORIE (ID_CATEGORIA),
   FOREIGN KEY (ID_SOTTOCATEGORIA) REFERENCES CMS_SOTTOCATEGORIE (ID_SOTTOCATEGORIA),
   FOREIGN KEY (ID_TIPO_ARTICOLO)  REFERENCES CMS_TIPI_ARTICOLO (ID_TIPO_ARTICOLO),
   FOREIGN KEY (ID_TIPO_PAGINA)    REFERENCES CMS_TIPI_PAGINA (ID_TIPO_PAGINA) );
   
CREATE TABLE CMS_PAGINE_DESC (
   ID_PAGINA          NUMBER(10)      NOT NULL,
   ID_LINGUA          NUMBER(10)      NOT NULL,
   DESCRIZIONE        VARCHAR2(255),
   CONSTRAINT PK_CMS_PAGINE_DESC PRIMARY KEY (ID_PAGINA, ID_LINGUA),
   FOREIGN KEY (ID_PAGINA) REFERENCES CMS_PAGINE (ID_PAGINA),
   FOREIGN KEY (ID_LINGUA) REFERENCES CMS_LINGUE (ID_LINGUA) );
   
CREATE TABLE CMS_PAGINE_ARTICOLI (
   ID_PAGINA          NUMBER(10)      NOT NULL,
   ID_ARTICOLO        NUMBER(10)      NOT NULL,
   ORDINE             NUMBER(10)      NOT NULL,
   CONSTRAINT PK_CMS_PAGINE_ARTICOLI PRIMARY KEY (ID_PAGINA, ID_ARTICOLO),
   FOREIGN KEY (ID_PAGINA)   REFERENCES CMS_PAGINE (ID_PAGINA),
   FOREIGN KEY (ID_ARTICOLO) REFERENCES CMS_ARTICOLI (ID_ARTICOLO) );

CREATE TABLE CMS_PAGINE_COMP (
   ID_PAGINA          NUMBER(10)      NOT NULL,
   ID_PAGINA_COMP     NUMBER(10)      NOT NULL,
   ORDINE             NUMBER(10)      NOT NULL,
   CONSTRAINT PK_CMS_PAGINE_COMP PRIMARY KEY (ID_PAGINA, ID_PAGINA_COMP),
   FOREIGN KEY (ID_PAGINA)      REFERENCES CMS_PAGINE (ID_PAGINA),
   FOREIGN KEY (ID_PAGINA_COMP) REFERENCES CMS_PAGINE (ID_PAGINA) );

CREATE TABLE CMS_LOG_VISITE (
   ID_ARTICOLO        NUMBER(10)      NOT NULL,
   DT_VISITA          TIMESTAMP       NOT NULL,
   ID_UTENTE          NUMBER(10)      NOT NULL
);

-- Indexes

CREATE INDEX IDX_AUTORI_COGNOME  ON CMS_AUTORI(COGNOME);
CREATE INDEX IDX_UTENTI_USERNAME ON CMS_UTENTI(USERNAME);
CREATE INDEX IDX_TAG_CODICE      ON CMS_TAG(CODICE);
CREATE INDEX IDX_LUOGHI_COM_CAP  ON CMS_LUOGHI(ID_COMUNE, CAP);

-- Sequences

CREATE SEQUENCE SEQ_CMS_LINGUE         START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_TAG            START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_CATEGORIE      START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_SOTTOCATEGORIE START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_ISTITUTI       START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_COMUNI         START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_LUOGHI         START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_RUOLI          START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_UTENTI         START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_AUTORI         START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_ARTICOLI       START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_ARTICOLI_MULT  START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_PAGINE         START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;

CREATE SEQUENCE SEQ_CMS_TIPI_UTENTE    START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_TIPI_ARTICOLO  START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_TIPI_LUOGO     START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_TIPI_AUTORE    START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_TIPI_CONTENUTO START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
CREATE SEQUENCE SEQ_CMS_TIPI_PAGINA    START WITH 1 MAXVALUE 999999999999 MINVALUE 1 NOCYCLE NOCACHE NOORDER;
