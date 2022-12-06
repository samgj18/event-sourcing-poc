# Lapsus Scala Template

⚠️ **IMPORTANT NOTICE** This is a template for backend applications that I found convenient given the stack choice in Lapsus. Feel free to use it as you see fit on your own personal projects or applications, don't forget to change `packageName` to the actual name of the package and also the `projectName` to the name of the new project.⚠️

## How to run?

```shell

sbt reStart

```

## Test

```shell

sbt test

```

## Starter

For **development**, this template contains the followings libraries and plugins, feel free to remove/add according to your use case:

>Plugins:

```
kindProjector,
betterMonadicFor,
semanticDB,
revolver
```

>Libraries:

```
cats,
catsEffect,
catsRetry,
circeCore,
circeGeneric,
circeParser,
circeRefined,
cirisCore,
cirisEnum,
cirisRefined,
derevoCore,
derevoCats,
derevoCirce,
fs2,
http4sDsl,
http4sServer,
http4sClient,
http4sCirce,
http4sJwtAuth,
javaxCrypto,
log4cats,
logback % Runtime,
monocleCore,
newtype,
redis4catsEffects,
redis4catsLog4cats,
refinedCore,
refinedCats,
skunkCore,
skunkCirce,
squants
```

As for **testing** it contains the following:

>Plugins:

```
kindProjector,
betterMonadicFor,
semanticDB,
revolver
```

>Libraries:

```
catsLaws,
log4catsNoOp,
monocleLaw,
refinedScalacheck,
weaverCats,
weaverDiscipline,
weaverScalaCheck
```
