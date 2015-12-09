-- noinspection SqlResolveForFile

--continents
SELECT
    continent.id AS "/",
    continent.*
FROM continent;

--continentsWithFacts
SELECT
    continent.id      AS "/",
    continent.*,
    continent_fact.id AS "/facts",
    continent_fact.*
FROM continent
    LEFT JOIN continent_fact ON continent.id = continent_fact.continent_id;

--continentsWithFactsAndCountries
SELECT
    continent.id      AS "/",
    continent.*,
    continent_fact.id AS "/facts",
    continent_fact.*,
    country.id        AS "/countries",
    country.*
FROM continent
    LEFT JOIN continent_fact ON continent.id = continent_fact.continent_id
    LEFT JOIN country ON continent.id = country.continent_id;


--continentsWithFactsAndCountriesAndCities_1
SELECT
    continent.id      AS "/",
    continent.*,
    continent_fact.id AS "/facts",
    continent_fact.*,
    country.id        AS "/countries",
    country.*,
    city.id           AS "/countries/cities",
    city.*
FROM continent
    LEFT JOIN continent_fact ON continent.id = continent_fact.continent_id
    LEFT JOIN country ON continent.id = country.continent_id
    LEFT JOIN city ON country.id = city.country_id;

--continentsWithFactsAndCountriesAndCities_2
SELECT
    continent.id      AS "     /       ",
    continent.*,
    continent_fact.id AS "/       facts    ",
    continent_fact.*,
    country.id        AS "         /countries",
    country.*,
    city.id           AS "  /  countries  /  cities  ",
    city.*
FROM continent
    LEFT JOIN continent_fact ON continent.id = continent_fact.continent_id
    LEFT JOIN country ON continent.id = country.continent_id
    LEFT JOIN city ON country.id = city.country_id;



