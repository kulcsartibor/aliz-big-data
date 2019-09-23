WITH
t1 AS (SELECT * REPLACE( PARSE_DATE('%Y%m%d',  web_analytics.date) AS date ) FROM ecommerce.web_analytics),
t2 AS (SELECT *, DATE_DIFF(date, DATE '2016-01-01', ISOWEEK) as week FROM t1),
weekly_summary AS (
  SELECT t2.fullVisitorId AS fullVisitorId, p.productSKU AS productSKU, p.v2productName AS productName,
  t2.week as week,
  SUM(p.productQuantity) AS quantity,
  SUM(p.productQuantity * p.productPrice) AS totalValue,
  MAX(date) as lastWeek
  FROM t2
  CROSS JOIN UNNEST(hits) as h
  CROSS JOIN UNNEST(h.product) as p
  WHERE h.ecommerceaction.action_type = '6' AND p.productQuantity > 0
  GROUP BY fullVisitorId, productSKU, productName, week
  ORDER BY fullVisitorId, productSKU, productName, week
  ),
grouped_by_week AS (
  SELECT
    (week -DENSE_RANK() OVER (PARTITION BY fullVisitorId, productSKU ORDER BY week)) AS detected_group,
    week, fullVisitorId, productSKU, productName, quantity, totalValue, lastWeek
  FROM weekly_summary
  ),
priod_detected AS (
  SELECT(max(week)- min(week)) AS period_length, fullVisitorId, productSKU, productName, sum(quantity) as quantity, sum(totalValue) AS totalValue, max(lastWeek) as lastWeek
  FROM grouped_by_week
  GROUP BY fullVisitorId, productSKU, productName, detected_group
)

SELECT fullVisitorId, ARRAY_AGG(STRUCT(productSKU, productName, quantity, period_length + 1 AS consecutiveWeeksCount, lastWeek)) AS products
FROM priod_detected
WHERE period_length > 0
GROUP BY fullVisitorId