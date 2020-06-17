// Extracts country from Address
address = execution.getVariableTyped('newAddress').getValue()
name = address.prop("countryIso").stringValue()
System.out.println("New country: " + name)
name
