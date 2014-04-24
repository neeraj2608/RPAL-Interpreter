Notes for TA:

1. To run the p2 script:
(from ANY directory)
p2 -ast <filename>
p2 -l <filename>
etc.

2. To run the project:
(from this directory)
make run cmd='-ast <filename>'
make run cmd='-l <filename>'

(from ANY OTHER directory)
make run cmd='-ast <filename>' -C <path_to_this_directory>
make run cmd='-l <filename>' -C <path_to_this_directory>

3. To make the project:
(from this directory)
make

(from ANY OTHER directory)
make -C <path_to_this_directory>

4. To run tests:
(from this directory)
make test

(from ANY OTHER directory)
make test -C <path_to_this_directory>

5. To clean the project:
(from this directory)
make cl

(from ANY OTHER directory)
make cl -C <path_to_this_directory>
