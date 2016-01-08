--name: get-deviceheat-by-uid-latest
SELECT * FROM deviceheat WHERE uid = :uid ORDER BY createdatetime DESC LIMIT 1

--name: get-uid-all-deviceheat
SELECT * FROM deviceheat WHERE uid= :uid ORDER BY createdatetime DESC

--name: get-all-deviceheat-latest
SELECT DISTINCT ON(uid) * FROM deviceheat ORDER BY uid,createdatetime DESC

--name: insert-deviceheat
INSERT INTO deviceheat(uid,temperature,humidity,createdatetime)
VALUES(:uid,:temperature,:humidity,now()) RETURNING id;
