  DELETE FROM MIS_IPAD_PROXYSERVER2..SYNC_CONFIG WHERE PROPERTY_KEY = 'ALPHA_GENERATOR_D' OR PROPERTY_KEY = 'ALPHA_GENERATOR_DB'
  GO
  INSERT INTO MIS_IPAD_PROXYSERVER2..SYNC_CONFIG VALUES('ALPHA_GENERATOR_DB','MIS_IPAD_GENERATOR','The name of database in Alpha used by Generator')
  GO