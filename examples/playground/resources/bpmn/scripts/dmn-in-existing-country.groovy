// Extracts country from Address
address = execution.getVariableTyped('existingAddress').getValue()
name = address.prop("countryIso").stringValue()
System.out.println("Existing country: " + name)
name
