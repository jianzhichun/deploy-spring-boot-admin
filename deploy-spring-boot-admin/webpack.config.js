'use strict';

var Webpack = require('webpack'),
  NgAnnotatePlugin = require('ng-annotate-webpack-plugin'),
  CopyWebpackPlugin = require('copy-webpack-plugin'),
  CleanWebpackPlugin = require('clean-webpack-plugin'),
  ExtractTextPlugin = require('extract-text-webpack-plugin'),
  path = require('path'),
  glob = require('glob');

var isDevServer = path.basename(require.main.filename) === 'webpack-dev-server.js';
var DIST = path.resolve(__dirname, 'target/dist');
var ROOT = __dirname;

var allModules = glob.sync(ROOT + '/src/main/webpack/*/module.js').map(function (file) {
  var name = /src\/main\/webpack\/([^\/]+)\/module\.js/.exec(file)[1];
  // name = 'deploy/' + name;

  return {
    name: name,
    bundle: name + '/module',
    entry: './' + path.relative(ROOT, file),
    outputPath: name + '/'
  };
});

var getEntries = function (modules) {
  var entries = { 'deploy/module': './src/main/webpack/module.js' };
  modules.forEach(function (module) {
    entries[module.bundle] = module.entry;
  });
  return entries;
};

var ConcatSource = require('webpack-sources').ConcatSource;
var ModuleConcatPlugin = function (files) {
  this.files = files;
};
ModuleConcatPlugin.prototype.apply = function (compiler) {
  var self = this;
  compiler.plugin('emit', function (compilation, done) {
    self.files.forEach(function (file) {
      var newSource = new ConcatSource();
      Object.keys(compilation.assets).forEach(function (asset) {
        if (file.test.test(asset)) {
          newSource.add(compilation.assets[asset]);
          newSource.add(file.delimiter);
        }
      });
      if (newSource.children.length > 0) {
        compilation.assets[file.filename] = newSource;
      }
    });
    done();
  });
};

module.exports = {
  context: ROOT,
  entry: getEntries(allModules),
  output: {
    path: DIST,
    filename: '[name].js'
  },
  externals: ['angular'],
  module: {
    preLoaders: [{
      test: /\.js$/,
      loader: 'eslint',
      exclude: [/node_modules/]
    }],
    loaders: [
      {
        test: /\.js$/,
        exclude: [/node_modules/],
        loader: 'ng-annotate'
      }, {
        test: /\.tpl\.html$/,
        loader: 'raw'
      }, {
        test: /\.css(\?.*)?$/,
        loader: ExtractTextPlugin.extract('style', 'css?-minimize')
      }
    ]
  },
  plugins: [
    new CleanWebpackPlugin([DIST]),
    new ExtractTextPlugin('[name]/module.css'),
    new NgAnnotatePlugin({ add: true }),
    new CopyWebpackPlugin([{
      from: '**/*.html',
      context: 'src/main/webpack'
    }
    ], { ignore: ['*.tpl.html'] })
  ].concat(!isDevServer ? [] : new ModuleConcatPlugin([
    {
      filename: 'deploy-all-modules.js',
      test: /module\.js/,
      delimiter: ';\n'
    }
  ])),
  devServer: {
    proxy: [
      {
        context: '/',
        target: 'http://localhost:8080',
        secure: false,
        onProxyRes: function (proxyRes, req, res) {
          if (req.path === '/all-modules.js') {
            delete proxyRes.headers['content-length'];
            proxyRes.headers['transfer-encoding'] = 'chunked';
            proxyRes.__pipe = proxyRes.pipe;
            proxyRes.pipe = function (sink, options) {
              var opts = options || {};
              opts.end = false;
              proxyRes.__pipe(sink, opts);
            };
            var suffixModule = '\n';
            require('http').get('http://localhost:9090/deploy-all-modules.js', function (r) {
              r.on('data', function (chunk) {
                suffixModule += chunk;
              });
              r.on('end', function () {
                setTimeout(function () { res.end(suffixModule) }, 1000);
              });
            });

          }

        }
      }
    ]
  },
  node: {
    fs: 'empty'
  }
};
