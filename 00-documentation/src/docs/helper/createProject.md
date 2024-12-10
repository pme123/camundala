# Create Project

**Experimental**

Creating a Project is done in two steps:

1. Create the project directory and the Helper Script.
2. Run the Helper Script to create the project structure.

Here we focus on the first step.

1. We can use the same `helperCompany.scala` script, we created in the [Init Company] step.
    ```scala
    cd ~/dev-myCompany
    ./helperCompany.scala project myProject
    ```
    This creates:
    
    ```bash
    dev-myCompany
      |  projects
      |    |  myProject
      |    |    |  helper.scala
    ``` 
   
1. Make `helper.scala` executable:
```bash
cd ~/projects/myProject
chmod +x helper.scala
```

1. Open the `myCompany-myProject` directory with your IDE (I use Intellij).

@:callout(info)
If you haven't released `company-camundala` yet,
you need to run it at least locally (`sbt publishLocal`) 
and set the version in the `helper.scala` manually.

```scala
//> using dep myCompany::myCompany-camundala-helper:VERSION NOT FOUND 
// replace with:
//> using dep myCompany::myCompany-camundala-helper:0.1.0-SNAPSHOT
```

@:@

### Next Step: [Project Development]

