module.exports = function(grunt) {
  grunt.initConfig({
    node: './node_modules',
    dest: './src/main/webapp/_res/node',
    destJs: '<%= dest %>/js',
    destCss: '<%= dest %>/css',

    clean: {
      build: { src: ['<%= dest %>'] }
    },

    mkdir: {
      all: {
        options: {
          create: [
            '<%= destCss %>/jquery-ui/images',
            '<%= destJs %>/jquery-ui/widgets'
          ]
        }
      }
    },

    copy: {
      main: {
        files: [
          // ---------------- CSS ----------------
          {
            expand: true,
            flatten: true,
            src: [
              '<%= node %>/bootstrap/dist/css/bootstrap*min*',
              '<%= node %>/xterm/css/xterm.*'
            ],
            dest: '<%= destCss %>/',
            filter: 'isFile'
          },
          {
            expand: true,
            flatten: true,
            src: ['<%= node %>/jquery-ui/themes/base/*'],
            dest: '<%= destCss %>/jquery-ui/',
            filter: 'isFile'
          },
          {
            expand: true,
            flatten: true,
            src: ['<%= node %>/jquery-ui/themes/base/images/*'],
            dest: '<%= destCss %>/jquery-ui/images',
            filter: 'isFile'
          },

          // ---------------- JS Core Libraries ----------------
          {
            expand: true,
            flatten: true,
            src: [
              '<%= node %>/jquery/dist/jquery.min.*',
              '<%= node %>/@popperjs/core/dist/umd/popper.min.js',
              '<%= node %>/@popperjs/core/dist/umd/popper.min.js.map',
              '<%= node %>/bootstrap/dist/js/bootstrap.min.*',
              '<%= node %>/floatthead/dist/jquery.floatThead.min.*',
              '<%= node %>/xterm/lib/xterm.*',
              '<%= node %>/xterm-addon-fit/lib/xterm-addon-fit.*'
            ],
            dest: '<%= destJs %>/',
            filter: 'isFile'
          },

          // ---------------- jQuery UI (Updated for v1.14.x) ----------------
          {
            expand: true,
            flatten: true,
            src: [
              '<%= node %>/jquery-ui/dist/*.js'
            ],
            dest: '<%= destJs %>/jquery-ui/',
            filter: 'isFile'
          },
{
  expand: true,
  cwd: '<%= node %>/jquery-ui/ui/',
  src: ['**/*', '**/.*'],
  dest: '<%= destJs %>/jquery-ui/',
  filter: 'isFile'
}
        ]
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-mkdir');
  grunt.loadNpmTasks('grunt-contrib-copy');

  grunt.registerTask('default', ['clean', 'mkdir', 'copy']);
};
