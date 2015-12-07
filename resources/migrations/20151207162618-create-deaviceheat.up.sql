-- Name: deaviceheat; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
CREATE TABLE IF NOT EXISTS  deviceheat
(
 id serial NOT NULL,
  uid text,
  temperature numeric,
  uv numeric,
  createdatetime TIMESTAMP WITH TIME ZONE,
  CONSTRAINT "deviceheat_pkey" PRIMARY KEY (id)
);
