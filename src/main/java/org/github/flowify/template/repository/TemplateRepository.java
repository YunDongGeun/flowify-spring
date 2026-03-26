package org.github.flowify.template.repository;

import org.github.flowify.template.entity.Template;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TemplateRepository extends MongoRepository<Template, String> {

    List<Template> findByCategory(String category);

    List<Template> findByIsSystem(boolean isSystem);
}