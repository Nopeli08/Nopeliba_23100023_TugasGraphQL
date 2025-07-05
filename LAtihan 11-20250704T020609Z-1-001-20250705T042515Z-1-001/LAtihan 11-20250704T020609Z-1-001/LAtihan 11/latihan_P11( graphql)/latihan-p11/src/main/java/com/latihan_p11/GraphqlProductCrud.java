package com.latihan_p11;

import static spark.Spark.*;
import com.google.gson.*;
import graphql.*;

public class GraphqlProductCrud {

   public static void main(String[] args) throws Exception {
       GraphQL graphql = GraphQLConfig.init();
       Gson gson = new Gson();

       port(4567);
       post("/graphql", (req, res) -> {
           res.type("application/json");

           JsonObject request = gson.fromJson(req.body(), JsonObject.class);
           String query = request.get("query").getAsString();

           ExecutionResult result = graphql.execute(query);
           return gson.toJson(result.toSpecification());
       });  
   }
}
