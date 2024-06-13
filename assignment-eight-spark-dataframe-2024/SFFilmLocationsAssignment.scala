// Databricks notebook source
import org.apache.spark.sql.SparkSession

// Initialize Spark session
val spark = SparkSession.builder
  .appName("Film Locations in San Francisco")
  .getOrCreate()



// COMMAND ----------

// MAGIC %md #####Note: You have the freedom to use SQL statements, or DataFrame/Dataset operations to do the tasks 

// COMMAND ----------

// MAGIC %md (2 marks) Task 1. Read in the "Film Locations in San Francisco" data file and make it to a DataFrame called sfflDF.

// COMMAND ----------

// Read the CSV file into a DataFrame
val sfflDF = spark.read
  .option("header", "true")  // The CSV file has a header row
  .option("inferSchema", "true")  // Infer the schema automatically
  .csv("/FileStore/tables/Film_Locations_in_San_Francisco-2.csv")
// Cache the DataFrame
sfflDF.cache()
// Show the DataFrame
sfflDF.show()


// COMMAND ----------

// MAGIC %md (2 marks) Task 2. Print the schema of sfflDF and show the first ten records.

// COMMAND ----------

// Print the schema of the DataFrame
sfflDF.printSchema()

// Show the first ten records of the DataFrame
sfflDF.show(10)

// COMMAND ----------

// MAGIC %md (1 marks) Task 3. Count the number of records in sfflDF.

// COMMAND ----------

// Count the number of records in the DataFrame
val recordCount = sfflDF.count()

// Print the record count
println(s"The number of records in sfflDF is: $recordCount")

// COMMAND ----------

// MAGIC %md (4 marks) Task 4. Filter out the records where "director" is null, or there two or more director names (indicated by "and" or "&") in the "director" column, or "locations" is null, or "release year" is not a number (i.e. containing non-digit characters), and call the new DataFrame fsfflDF. 

// COMMAND ----------

import org.apache.spark.sql.functions._

val fsfflDF = sfflDF.filter(
  col("Director").isNotNull &&
  !col("Director").contains("and") &&
  !col("Director").contains("&") &&
  col("Locations").isNotNull &&
  col("Release Year").cast("string").rlike("^[0-9]+$")
)
// Show the first ten records of the DataFrame
fsfflDF.show(10)

// COMMAND ----------

// MAGIC %md (2 marks) Task 5. Add a new column called "trimed_title" to fsfflDF that contains the title of films with the space trimed from the left and right end, drop the old "title" column and rename "trimed_title" column to "title", and call this new DataFrame csfflDF. 

// COMMAND ----------

// Add the new column "trimed_title" with trimmed titles, drop the old "Title" column and rename "trimed_title" to "Title"
val csfflDF = fsfflDF
  .withColumn("trimed_title", trim(col("Title")))
  .drop("Title")
  .withColumnRenamed("trimed_title", "Title")

csfflDF.cache()

// COMMAND ----------

// MAGIC %md #####Note: You will use csfflDF  in the following tasks

// COMMAND ----------

// MAGIC %md (2 marks) Task 6. Show the title, release year of films that were released between 2000-2009 (inclusive), ordered by the release year (from latest to earliest).

// COMMAND ----------

csfflDF
  .filter(col("Release Year").between(2000, 2009))
  .select("Title", "Release Year")
  .distinct()
  .orderBy(col("Release Year").desc)
  .show(10)

// COMMAND ----------

// MAGIC %md (2 marks) Task 7. Show the title of film(s) written by Keith Samples (note: there could be more than one writer name in the "writer" column)

// COMMAND ----------

csfflDF.filter(col("writer").like("%Keith Samples%"))
  .select("Title")
  .distinct()
  .show(false)

// COMMAND ----------

// MAGIC %md (2 marks) Task 8. Show the earliest and latest release year. 

// COMMAND ----------

// Find the earliest and latest release year
csfflDF.agg(min("Release Year").alias("Earliest Release Year"), max("Release Year").alias("Latest Release Year"))
  .show()

// COMMAND ----------

// MAGIC %md (3 marks) Task 9. Count the number of films, the number of distinct production company, and the average number of films made by a production company

// COMMAND ----------

// Count the number of films
val numFilms = csfflDF.count()

// Count the number of distinct production companies
val numDistinctCompanies = csfflDF.select("Production Company").distinct().count()

// Calculate the average number of films made by a production company
val avgFilmsPerCompany = csfflDF.groupBy("Production Company").count().agg(avg("count")).first().getDouble(0)

// Display the results
println(s"Number of films: $numFilms")
println(s"Number of distinct production companies: $numDistinctCompanies")
println(f"Average number of films per production company: $avgFilmsPerCompany%.2f")

// COMMAND ----------

// MAGIC %md (3 marks) Task 10. Show the title of films that were shot at more than three locations (inclusive).  

// COMMAND ----------

csfflDF.groupBy("Title").agg(count("Locations").alias("Location Count"))
  .filter(col("Location Count") >= 3)
  .select("Title")
  .show(false)

// COMMAND ----------

// MAGIC %md (3 marks) Task 11. Add a new column called "Crew" to csfflDF that contains a list of people who had worked on the film (i.e. concatenate the column "Actor 1", "Actor 2", "Actor 3", "Director", "Distributor", Writer"). Show the title of films that Clint Eastwood were involved.

// COMMAND ----------

csfflDF.withColumn("Crew", array("Actor 1", "Actor 2", "Actor 3", "Director", "Distributor", "Writer"))
  .filter(array_contains(col("Crew"), "Clint Eastwood"))
  .select("Title")
  .distinct()
  .show(false)

// COMMAND ----------

// MAGIC %md (3 marks) Task 12. Show the number of films directed by each director, order by the number of films (descending).

// COMMAND ----------

// Group the DataFrame by director and count the number of films for each director
csfflDF.groupBy("Director").agg(count("Title").alias("Film Count"))
  .orderBy(col("Film Count").desc)
  .show(false)

// COMMAND ----------

// MAGIC %md (3 marks) Task 13. Show the number of films made by each production company and in each release year.

// COMMAND ----------

csfflDF.groupBy("Production Company", "Release Year")
  .agg(count("Title").alias("Film Count"))
  .orderBy(col("Production Company"), col("Release Year"))
  .show(true)


// COMMAND ----------

// MAGIC %md (3 marks) Task 14. Use csfflDF to generate a new DataFrame that has the column "title" and a new column "location_list" that contains an array of locations where the film was made, and then add a new column that contains the number of locations in "location_list". Show the first ten records of this dataframe.  

// COMMAND ----------

val filmsWithLocations = csfflDF
  .withColumn("Location List", split(col("Locations"), ","))
  .withColumn("Number Locations", size(col("Location List")))
  .groupBy("Title")
  .agg(collect_list("Location List").as("Location List"), sum("Number Locations").as("Total Locations"))

filmsWithLocations.show(10, true)

// COMMAND ----------

// MAGIC %md (3 marks) Task 15. Use csfflDF to generate a new DataFrame that has the "title" column, and two new columns called "Golden Gate Bridge" and "City Hall" that contains the number of times each location was used in a film. Show the title of films that had used each location at least once.
// MAGIC Hint: Use pivot operation  

// COMMAND ----------

// Split the Locations column into an array
val locationsDF = csfflDF.withColumn("location", explode(split(col("Locations"), ", ")))

// Pivot the data to get the count of each location for each film
val pivotDF = locationsDF.groupBy("Title").pivot("location", Seq("Golden Gate Bridge", "City Hall")).count().na.fill(0)
pivotDF.cache()

// Filter the DataFrame to show only the films that used each location at least once
val usedBridgeDF = pivotDF.filter(col("Golden Gate Bridge") > 0).select("Title")
val usedCityHallDF = pivotDF.filter(col("City Hall") > 0).select("Title")

println("Films that used Golden Gate Bridge:")
usedBridgeDF.show(false)

println("Films that used City Hall:")
usedCityHallDF.show(false)

// COMMAND ----------

// MAGIC %md (4 marks) Task 16. Add a new column to csfflDF that contains the names of directors with their surname uppercased.
// MAGIC Hint. Use a user defined function to uppercase the director's surname. Please refer to https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-udfs.html
// MAGIC //                                                                                              
// MAGIC  

// COMMAND ----------


// Define a UDF to uppercase the surname of the director
val upperCaseSurnameUDF = udf((fullName: String) => {
  val parts = fullName.split(" ")
  if (parts.length > 1) {
    val firstName = parts.head
    val lastName = parts.last.toUpperCase
    s"$firstName $lastName"
  } else {
    fullName.toUpperCase
  }
})

// Add a new column "Director Uppercased" containing the names of directors with their surname uppercased
csfflDF.withColumn("Director Uppercased", upperCaseSurnameUDF(col("director")))
  .show(true)

// COMMAND ----------

// MAGIC %md (4 marks) Task 17. Use csfflDF to generate a new DataFrame that has the column "Locations" and a new column "Actors" that contains a list of distinct actors who had cast in a location. 
// MAGIC Note: run "Hint" below if you need a bit help on this task  

// COMMAND ----------

// Define a function to combine collect_set results into a single list
def combineSets(a: Seq[Seq[String]]): Seq[String] = a.flatten.distinct

// Define a UDF to apply the combineSets function
val combineSetsUDF = udf(combineSets _)

// Group by Locations and aggregate the actors into sets
val groupedDF = csfflDF
  .groupBy("Locations")
  .agg(
    collect_set(col("Actor 1")).as("Actor1Set"),
    collect_set(col("Actor 2")).as("Actor2Set"),
    collect_set(col("Actor 3")).as("Actor3Set")
  )

// Apply the UDF to combine the sets into a single list
val combinedDF = groupedDF.withColumn("Actors", combineSetsUDF(array(col("Actor1Set"), col("Actor2Set"), col("Actor3Set"))))
  .select("Locations", "Actors")
  .show(false)

// COMMAND ----------

displayHTML("<p>One solution is groupBy on <i>Locations</i>, then collect_set on <i>Actor 1</i>, <i>Actor 2</i>, <i>Actor 3</i>, finally define a user function to combine <i>collect_set(Actor 1)</i>, <i>collect_set(Actor 2)</i>,  <i>collect_set(Actor 3)</i></p>")

// COMMAND ----------

// MAGIC %md (4 marks) Task 18. Show the names of people who is an actor (in one of "Actor 1", "Actor 2" and "Actor 3" column) and also a director (in the "Director" column) in the same film. Note: run "Hint" below if you need a bit help on this task

// COMMAND ----------

// Create DataFrames for Actor 1, Actor 2, Actor 3, and Director
val actor1DF = csfflDF.select(col("Actor 1").as("Name"), col("Title"))
val actor2DF = csfflDF.select(col("Actor 2").as("Name"), col("Title"))
val actor3DF = csfflDF.select(col("Actor 3").as("Name"), col("Title"))
val directorDF = csfflDF.select(col("Director").as("Name"), col("Title"))

// Combine the four DataFrames into a single DataFrame
val combinedDF = actor1DF.union(actor2DF).union(actor3DF).union(directorDF)

// Perform a self-join to find names that appear both as an actor and a director in the same film
val resultDF = combinedDF.as("a")
  .join(combinedDF.as("b"), col("a.Title") === col("b.Title") && col("a.Name") =!= col("b.Name"))
  .select(col("a.Name").as("Actor and Director"), col("a.Title"))
  .distinct()
  .show(false)

// COMMAND ----------

displayHTML("<p>One solution is use csfflDF to create four new DataFrames (for 'Actor 1', 'Actor 2', 'Actor 3' and 'Director') that contains two columns, the actor/director name and the film title, and then use 'union' and 'join' operations.</p>")
