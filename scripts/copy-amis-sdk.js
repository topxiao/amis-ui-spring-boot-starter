const fs = require('fs');
const path = require('path');

// Read amis version from package.json to avoid hardcoding
const pkg = JSON.parse(fs.readFileSync('package.json', 'utf8'));
const amisVersion = pkg.dependencies.amis.replace(/^[\^~>=<]*/, '');

const dest = path.join('src/main/resources/static/cdn/amis', amisVersion);

if (fs.existsSync(dest)) {
  fs.rmSync(dest, { recursive: true, force: true });
}
fs.mkdirSync(path.join(dest, 'umd'), { recursive: true });

function copyDir(src, dst) {
  fs.mkdirSync(dst, { recursive: true });
  fs.readdirSync(src).forEach(item => {
    const srcPath = path.join(src, item);
    const dstPath = path.join(dst, item);
    fs.statSync(srcPath).isDirectory()
      ? copyDir(srcPath, dstPath)
      : fs.copyFileSync(srcPath, dstPath);
  });
}

function stripSourcemap(file) {
  if (!fs.existsSync(file)) return;
  const content = fs.readFileSync(file, 'utf8');
  fs.writeFileSync(file, content.replace(/\/\/# sourceMappingURL=.*$/gm, ''));
}

copyDir(path.join('node_modules/amis/sdk'), dest);
fs.copyFileSync(
  'node_modules/history/umd/history.production.min.js',
  path.join(dest, 'umd/history.js')
);
stripSourcemap(path.join(dest, 'umd/history.js'));

console.log(`amis sdk v${amisVersion} copied without sourcemap`);
