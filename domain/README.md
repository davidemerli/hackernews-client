# Domain Module

This module holds all the application business logic.
This module follow the **dependency rule** and does not depend on anything from the **data** or **app** modules.

The domain modules also does not depend on any framework.

Here are present all UseCases and business models of the application.

**Repositories abstractions** are presented here, with their corresponding implementation in the data model.

This allows the domain module to communicate with concrete repositories without breaking the
dependency rule, through **Dependency Inversion Principle**.

All the logic of the application is abstracted in this module via UseCases, which are then implemented
in the **data** module, and used in the **app** module.

## Dependency Inversion

- High-level modules should not import anything from low-level modules. Both should depend on abstractions (e.g., interfaces).
- Abstractions should not depend on details. Details (concrete implementations) should depend on abstractions.

When designing the interaction between a high-level module and a low-level one, the interaction
should be thought of as an abstract interaction between them.

The low-level one should be designed with the interaction in mind.