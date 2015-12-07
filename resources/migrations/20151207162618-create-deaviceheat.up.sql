-- Name: deaviceheat; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
CREATE TABLE IF NOT EXISTS  deaviceheat
(
 id serial NOT NULL,
  uid text,
  heat numeric,
  uv numeric,
  createdatetime TIMESTAMP WITH TIME ZONE,
  CONSTRAINT "deaviceheat_pkey" PRIMARY KEY (id)
);
