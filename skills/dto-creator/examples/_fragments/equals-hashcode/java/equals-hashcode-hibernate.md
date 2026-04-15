# equals() and hashCode() with Hibernate proxy support (Java)

## Insert Point
As methods in class body, after getters/setters.

## When to use

Use this variant INSTEAD of `equals-hashcode.md` when **all** of:
- Hibernate is on the classpath (`presentDeps` contains `hibernate-core`).
- The user explicitly opted in to proxy-aware equals (variant question
  "Hibernate-proxy aware equals?" answered "yes").

If either condition is false, use the plain `equals-hashcode.md`.


## Code

```defaults
// Default: skip. Use only when conditions above are met.
```

```java
@Override
public boolean equals(Object o) {
	if (this == o) return true;
	if (o == null) return false;
	Class<?> oEffectiveClass = o instanceof org.hibernate.proxy.HibernateProxy
		? ((org.hibernate.proxy.HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
		: o.getClass();
	Class<?> thisEffectiveClass = this instanceof org.hibernate.proxy.HibernateProxy
		? ((org.hibernate.proxy.HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
		: this.getClass();
	if (thisEffectiveClass != oEffectiveClass) return false;
	{className} entity = ({className}) o;
	return java.util.Objects.equals(this.{field1}, entity.{field1}) &&
		java.util.Objects.equals(this.{field2}, entity.{field2});
}

@Override
public int hashCode() {
	return this instanceof org.hibernate.proxy.HibernateProxy
		? ((org.hibernate.proxy.HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
		: getClass().hashCode();
}
```

## FQN handling

Shorten `org.hibernate.proxy.HibernateProxy` in the body and add a single
`import org.hibernate.proxy.HibernateProxy;` line.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | DTO class name | -- |
| `{fieldN}` | field names from selected entity attributes | -- |
