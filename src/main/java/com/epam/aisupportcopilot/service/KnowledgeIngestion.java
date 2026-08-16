package com.epam.aisupportcopilot.service;

import java.io.IOException;

public interface KnowledgeIngestion {

    int ingestAllDocuments() throws IOException;
}