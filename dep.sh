gradle clean all
rm -rf repo

mkdir repo
git clone --single-branch --branch $env.BRANCH_NAME https://github.com/NBISweden/LocalEGA-deploy-swarm.git repo
gradle bootstrap
gradle deployPrivate