package com.latihan_p11;

import graphql.*;
import graphql.schema.idl.*;
import java.io.*;
import java.util.Objects;
import graphql.schema.GraphQLSchema;

public class GraphQLConfig {
    public static GraphQL init() throws IOException {
        InputStream schemaStream = GraphQLConfig.class.getClassLoader().getResourceAsStream("schema.graphqls");

        if (schemaStream == null) {
            throw new RuntimeException("schema.graphqls not found in classpath.");
        }

        String schema = new String(Objects.requireNonNull(schemaStream).readAllBytes());

        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schema);

        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
            .type("Query", builder -> builder
                .dataFetcher("allProducts", env -> ProductRepository.findAll())
                .dataFetcher("productById", env -> {
                    // Fetch ID as String and then convert it to Long if needed
                    String idStr = env.getArgument("id");
                    Long id = Long.valueOf(idStr);  // Convert String to Long
                    return ProductRepository.findById(id);
                })
            )
            .type("Mutation", builder -> builder
                .dataFetcher("addProduct", env -> ProductRepository.add(
                    env.getArgument("name"),
                    ((Number) env.getArgument("price")).doubleValue(),
                    env.getArgument("category")
                ))
                .dataFetcher("deleteProduct", env -> {
                    String idStr = env.getArgument("id");  // Fetch ID as String
                    Long id = Long.valueOf(idStr);  // Convert String to Long
                    return ProductRepository.delete(id);
                })
                .dataFetcher("updateProduct", env -> {
                    String idStr = env.getArgument("id");  // Fetch ID as String
                    Long id = Long.valueOf(idStr);  // Convert String to Long
                    String name = env.getArgument("name");
                    double price = ((Number) env.getArgument("price")).doubleValue();
                    String category = env.getArgument("category");
                    return ProductRepository.update(id, name, price, category);
                })
            )
            .build();

        GraphQLSchema schemaFinal = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        return GraphQL.newGraphQL(schemaFinal).build();
    }
}
