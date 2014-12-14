'use strict';

var path = require('path');

module.exports = function gruntFile(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        shell: {
            //application scripts
            serve: {
                command: ' java -cp lib/jsoup-1.8.1.jar:lib/trove-3.0.3.jar:src -Xmx4096m edu.nyu.cs.cs2580.SearchEngine \ --mode=serve --port=25810 --options=conf/engine.conf',
                options: {
                    async: false
                }
            },
            mine: {
                command: 'java lib/jsoup-1.8.1.jar:lib/trove-3.0.3.jar:src -Xmx2048m edu.nyu.cs.cs2580.SearchServer \ --mode=mining --options=conf/engine.conf',
                options: {
                    async: false
                }
            },
            index: {
                command: 'java -cp lib/jsoup-1.8.1.jar:lib/trove-3.0.3.jar:src -Xmx2048m edu.nyu.cs.cs2580.SearchEngine \ --mode=index --options=conf/engine.conf',
                options: {
                    async: false
                }
            },
            compile: {
                command: 'javac -Xlint -cp lib/Jsoup-1.8.1.jar:lib/trove-3.0.3.jar src/edu/nyu/cs/cs2580/*.java',
                options: {
                    async: false
                }
            },
            pythonServer: {
                command: 'python -m SimpleHTTPServer',
                options: {
                    async: false
                }
            },
            pythonKill: {
                command: 'killall python',
                options: {
                    async: false
                }
            }
        },

        open : {
            search: {
                app: 'Google Chrome',
                path: 'http://localhost:8000/client/src/main'
            }
        }
    });

    grunt.loadNpmTasks('grunt-shell-spawn');
    grunt.loadNpmTasks('grunt-open');

    grunt.registerTask('compile', [
        'shell:compile'
    ]);

    grunt.registerTask('serve', [
       'shell:serve'
    ]);

    grunt.registerTask('mine', [
        'shell:mine'
    ]);

    grunt.registerTask('index', [
        'shell:index'
    ]);

    grunt.registerTask('client', [
        'shell:pythonServer'
    ]);
};
