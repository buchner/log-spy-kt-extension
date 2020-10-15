# Sign the CLA
Please execute the following steps to sign the CLA.
1. Pick the proper CLA.
1. Read the CLA.
1. File a pull request.

## Pick the CLA
If you are an individual who wants to contribute, the proper CLA is the
[Individual CLA](cla/individual-cla.md). If you are contributing on behalf of your employer,
or as part of your role as an employee, the proper CLA is the [Entity CLA](cla/entity-cla.md).

## File the PR
Make a pull request (PR) that only adds a single file to the [contributors](contributors/)
directory. Name the file with the same name as your GitHub user id. E.g. if your user id
is `foobar` the full path to the file would be `contributors/foobar.md`.

Put the following content in the file.
```
[date]

I hereby agree to the terms of the [cla type] Contributors License with SHA512 checksum
[checksum].

I furthermore declare that I am authorized and able to make this
agreement and sign this declaration.

Signed,

[your name]
https://github.com/[your github userid]
```

Replace the brackets as follows.

| Bracket | Replacement |
| --- | --- |
| date | today's date |
| cla type | based on what you picked in step one either `Individual` or `Entity` |
| checksum | `85919842ba41f4fb8a88655244108cb27f60cfcf38a9e3f7af1e4ade774434ece6478993da8ce75b4fb404e6720947a89e18414d3574524bd439a20e05e4a338` for the individual cla, `27ab29d910422124dd166ccc007b45219bb2e5083afba0a85c52203faa748d5223e8f3e66d84e01fd4dac1434d1e3bf68d3d06f7db909466aef75fcb34c12853` for the entity cla |
| your name | your name as it can be found in official documents |
| your github userid | your GitHub user id e.g. in the example above `foobar` |

You can confirm the checksum of the CLA you picked by running a sha512 over `individual-cla.md`/`entity-cla.md`.
```
sha512sum individual-cla.md
sha512sum entity-cla.md
```

If the output is different from above, do not sign the CLA and file an issue.
