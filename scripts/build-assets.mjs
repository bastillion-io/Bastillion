import {
  copyFileSync,
  mkdirSync,
  readdirSync,
  rmSync,
  statSync
} from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const projectRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const modulesDir = join(projectRoot, 'node_modules');
const destination = join(projectRoot, 'src/main/webapp/_res/node');
const cssDestination = join(destination, 'css');
const jsDestination = join(destination, 'js');

function copyFile(source, target) {
  mkdirSync(dirname(target), { recursive: true });
  copyFileSync(source, target);
}

function copyDirectoryFiles(sourceDir, targetDir, predicate = () => true) {
  for (const entry of readdirSync(sourceDir)) {
    const source = join(sourceDir, entry);
    if (statSync(source).isFile() && predicate(entry)) {
      copyFile(source, join(targetDir, entry));
    }
  }
}

function copyDirectoryRecursive(sourceDir, targetDir) {
  for (const entry of readdirSync(sourceDir)) {
    const source = join(sourceDir, entry);
    const target = join(targetDir, entry);
    if (statSync(source).isDirectory()) {
      copyDirectoryRecursive(source, target);
    } else {
      copyFile(source, target);
    }
  }
}

rmSync(destination, { recursive: true, force: true });
mkdirSync(join(cssDestination, 'jquery-ui/images'), { recursive: true });
mkdirSync(join(jsDestination, 'jquery-ui/widgets'), { recursive: true });

copyDirectoryFiles(
  join(modulesDir, 'bootstrap/dist/css'),
  cssDestination,
  name => name.startsWith('bootstrap') && name.includes('min')
);
copyFile(join(modulesDir, '@xterm/xterm/css/xterm.css'), join(cssDestination, 'xterm.css'));
copyDirectoryFiles(join(modulesDir, 'jquery-ui/themes/base'), join(cssDestination, 'jquery-ui'));
copyDirectoryFiles(
  join(modulesDir, 'jquery-ui/themes/base/images'),
  join(cssDestination, 'jquery-ui/images')
);

copyDirectoryFiles(join(modulesDir, 'jquery/dist'), jsDestination, name => name.startsWith('jquery.min.'));
copyFile(join(modulesDir, '@popperjs/core/dist/umd/popper.min.js'), join(jsDestination, 'popper.min.js'));
copyFile(join(modulesDir, '@popperjs/core/dist/umd/popper.min.js.map'), join(jsDestination, 'popper.min.js.map'));
copyDirectoryFiles(
  join(modulesDir, 'bootstrap/dist/js'),
  jsDestination,
  name => name.startsWith('bootstrap.min.')
);
copyDirectoryFiles(
  join(modulesDir, 'floatthead/dist'),
  jsDestination,
  name => name.startsWith('jquery.floatThead.min.')
);
copyDirectoryFiles(join(modulesDir, '@xterm/xterm/lib'), jsDestination, name => name.startsWith('xterm.js'));
copyDirectoryFiles(
  join(modulesDir, '@xterm/addon-fit/lib'),
  jsDestination,
  name => name.startsWith('addon-fit.js')
);

copyDirectoryFiles(
  join(modulesDir, 'jquery-ui/dist'),
  join(jsDestination, 'jquery-ui'),
  name => name.endsWith('.js')
);
copyDirectoryRecursive(join(modulesDir, 'jquery-ui/ui'), join(jsDestination, 'jquery-ui'));

console.log('Frontend assets built successfully.');
