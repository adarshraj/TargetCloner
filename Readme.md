# Target Generator
- This is a tool to generate target.xml for the given p2 repository in the input file.
- This tool is custom-made for a specific project and may not be useful for other projects but can be used as a reference.
- Requires a delivery report and a list of p2 repositories to generate the target.xml
- Both the input and output are in XML format based on the schema defined in the project.
- The tool can be run as a normal java application.
- The tool is tested on Windows with Java 21 with custom inputs. Should work in Java 17.

# How to use
- The tool is a command line tool and requires the following inputs:
  - Delivery report file
  - List of p2 repositories
  - The input.xml needs be placed in the input folder and the .target file created for each component is saved on to output folder

# Pending
- The tool is not complete and is still under development. But there won't be any major changes to the code.
- The tool is not yet tested on Linux or Mac.
- The command line arguments are not yet implemented.
- The tool is not yet tested with Java 17.
- Junit tests are not yet written.
- The tool is not yet tested with a large number of p2 repositories.
- The tool is not yet tested with a large delivery report.
- The tool is not yet tested with a large number of components.
- The tool is not yet tested with a large number of dependencies.

