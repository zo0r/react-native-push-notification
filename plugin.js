const Path = require("path");
const fs = require("fs");
const wd = process.cwd();
module.exports = [
  {
    name: "add-google-services <path>",
    description: "Add google-services.json file from path <path>",
    func: (args, opts) => {
      const googpath = args[0];
      if (fs.existsSync(googpath)) {
        const targetPath = Path.join(
          wd,
          "android",
          "app",
          "google-services.json"
        );
        fs.copyFileSync(googpath, targetPath);
        console.log("Successfully populated ", targetPath);
      } else {
        console.log("Identified path does not exist: ", googpath, "Aborting.");
      }
    }
  },
  {
    name: "set-application-id <newid>",
    description:
      "Set the android application ID in build.gradle to newid. (Usually to match your google-services)",
    func: (args, opts) => {
      const targetPath = Path.join(wd, "android", "app", "build.gradle");
      if (!fs.existsSync(targetPath)) {
        console.log("Could not find ", targetPath, "Aborting");
        process.exit(1);
      }
      const t = fs.readFileSync(targetPath, { encoding: "UTF8" });
      var lines = t.split("\n");
      const keyLine = lines.findIndex(line => {
        return line.indexOf("applicationId ") > -1;
      });
      if (keyLine) {
        lines[keyLine] = 'applicationId "' + args[0] + '"';
        const outtext = lines.join("\n");
        fs.writeFileSync(targetPath, outtext);
        console.log(
          "Successfully updated applicationId to ",
          args[0],
          "in",
          targetPath
        );
      } else {
        console.log("Could not find applicationId in ", targetPath);
        process.exit(1);
      }
    }
  }
];
