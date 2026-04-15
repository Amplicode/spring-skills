# Method Naming Conventions

## Default patterns

| Method type | Pattern | Example (Entity=Order, DTO=OrderDto) |
|-------------|---------|--------------------------------------|
| toEntity | `"toEntity"` | `toEntity` |
| toDto | `"to${DTO_NAME}"` | `toOrderDto` |
| partialUpdate | `"partialUpdate"` | `partialUpdate` |
| updateWithNull | `"updateWithNull"` | `updateWithNull` |

`${DTO_NAME}` is replaced with the DTO short class name (e.g. `OrderDto`).

## Parameter naming

| Parameter | Rule | Example |
|-----------|------|---------|
| entity param | decapitalized entity short name | `order` |
| DTO param | decapitalized DTO short name | `orderDto` |

## Collection method naming strategy

Default: LIKE_SINGLE (same name as single-item method).

Other strategies (configurable, but rarely changed):
- PLURALIZE: `toEntities`, `toOrderDtos`
- COLLECTION_TYPE: `toEntityList`, `toOrderDtoList`

## Custom extension function naming (Kotlin Custom mapper)

- toDto: `fun EntityFqn.toOrderDto()` -- extension on entity
- toEntity: `fun DtoFqn.toEntity()` -- extension on DTO
- updateWithNull: `fun EntityFqn.updateWithNull(orderDto: DtoFqn)` -- extension on entity with DTO param
