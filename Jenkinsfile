node {
	def gitBaseAddress = "github.com"
	def gitBuildAddress = "${gitBaseAddress}/tmax-cloud/template-service-broker.git"
	
	def hcBuildDir = "/var/lib/jenkins/workspace/template-service-broker"
	def imageBuildHome = "/root/HyperCloud-image-build/template-service-broker"

	def version = "${params.majorVersion}.${params.minorVersion}.${params.tinyVersion}.${params.hotfixVersion}"
	def preVersion = "${params.majorVersion}.${params.minorVersion}.${params.tinyVersion}.${params.preHotfixVersion}"
	def imageTag = "b${version}"
	def binaryHome = "${hcBuildDir}/build"
	def scriptHome = "${hcBuildDir}/scripts"
		
	def credentialsId = "hypercloud_github"
	def userName = "seonho_choi"
	def userEmail = "seonho_choi@tmax.co.kr"

    stage('gradle build') {
        //deleteDir()
		new File("${hcBuildDir}").mkdir()
		dir ("${hcBuildDir}") {
			git branch: "${params.buildBranch}",
			credentialsId: '${credentialsId}',
			url: "https://${gitBuildAddress}"
		}
		gradleDoBuild("${hcBuildDir}")
    }

    stage('file home copy') {
		sh "sudo rm -rf ${imageBuildHome}/service-broker/"
		sh "sudo rm -f ${imageBuildHome}/start.sh"
		sh "sudo cp -r ${binaryHome}/service-broker ${imageBuildHome}/service-broker"
		sh "sudo cp ${binaryHome}/start.sh ${imageBuildHome}/start.sh"
    }
    
	stage('make change log'){
		sh "sudo sh ${scriptHome}/hypercloud-make-changelog.sh ${version} ${preVersion}"
	}
	
	stage('build & push image'){
		sh "sudo docker build --tag tmaxcloudck/template-service-broker:${imageTag} ${imageBuildHome}/"
		sh "sudo docker push tmaxcloudck/template-service-broker:${imageTag}"
	}
	
	stage('git commit & push'){
		dir ("${hcBuildDir}") {
			sh "git checkout ${params.buildBranch}"

			sh "git config --global user.name ${userName}"
			sh "git config --global user.email ${userEmail}"
			sh "git config --global credential.helper store"

			sh "git add -A"

			sh (script:'git commit -m "[Version-Up] make changelog" || true')
			sh "git tag v${version}"
			
			sh "sudo git push -u origin +${params.buildBranch}"
			sh "sudo git push origin v${version}"

			sh "git fetch --all"
			sh "git reset --hard origin/${params.buildBranch}"
			sh "git pull origin ${params.buildBranch}"
		}	
	}
	stage('clean repo'){
		sh "sudo rm -rf ${hcBuildDir}/*"
	}
}

void gradleDoBuild(dirPath) {
    sh "./gradlew clean doBuild"
}