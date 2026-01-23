pipeline {
  agent {
    node {
      label 'maven'
    }

  }
  parameters {
    string(name: 'PROJECT_VERSION', defaultValue: 'v1.0', description: '')
    string(name: 'PROJECT_NAME', defaultValue: '', description: '')
  }
  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITEE_CREDENTIAL_ID = 'gitee-id'
    KUBECONFIG_CREDENTIAL_ID = 'sangomall-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = '1191082340'
    GITEE_ACCOUNT = 'zhuyanghua2003'
    SONAR_CREDENTIAL_ID = 'sonar-qube'
  }
  stages {
    stage('拉取项目源码') {
      agent none
      steps {
        git(url: 'https://github.com/zhuyanghua2003/mail.git', credentialsId: 'github-id', branch: 'main', changelog: true, poll: false)
      }
    }

    stage('代码质量检查和分析') {
      agent none
      steps {
        container('maven') {
          withCredentials([string(credentialsId: 'sonar-qube', variable: 'SONAR_TOKEN')]) {
            withSonarQubeEnv('sonar') {
              sh 'echo 当前目录 `pwd`'
              sh 'mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn_settings.xml'
              sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar -gs `pwd`/mvn_settings.xml -Dsonar.login=$SONAR_TOKEN'
            }

          }

          timeout(unit: 'HOURS', activity: false, time: 1) {
            waitForQualityGate 'true'
          }

        }

      }
    }

    stage('单元测试') {
      agent none
      steps {
        container('maven') {
          sh 'mvn clean package -Dmaven.test.skip=true -gs `pwd`/mvn_settings.xml'
        }

      }
    }

    stage('构建项目容器镜像及推送容器镜像') {
      agent none
      steps {
        container('maven') {
          sh 'mvn clean package -Dmaven.test.skip=true -gs `pwd`/mvn_settings.xml'
          sh 'cd $PROJECT_NAME && docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BUILD_NUMBER .'
          withCredentials([usernamePassword(credentialsId: 'dockerhub-id', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
            sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
            sh 'docker push $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BUILD_NUMBER'
            sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
            sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
          }

        }

      }
    }

    stage('创建项目代码以及容器镜像的发布版') {
      agent none
      when {
        expression {
          return params.PROJECT_VERSION =~ /v.*/
        }

      }
      steps {
        container('maven') {
          input(message: '是否提交带有tag发布版本的容器镜像', submitter: 'project-admin')
          withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh-id', keyFileVariable: 'GITHUB_SSH_KEY')]) {
            sh 'git config --global user.email "1191082340@qq.com"'
            sh 'git config --global user.name "zhuyanghua2003"'
            sh 'git tag -a $PROJECT_VERSION -m "$PROJECT_VERSION"'
            sh 'mkdir -p ~/.ssh && cp $GITHUB_SSH_KEY ~/.ssh/id_ed25519 && chmod 600 ~/.ssh/id_ed25519'
            sh 'ssh-keyscan github.com >> ~/.ssh/known_hosts'
            sh 'git remote set-url origin git@github.com:zhuyanghua2003/mail.git'
            sh 'git push origin --tags --ipv4'
          }

          sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION'
          sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION'
        }

      }
    }

    stage('部署微服务项目到k8s集群') {
      agent none
      steps {
        input(message: '是否允许发布微服务项目到k8s集群', submitter: 'project-admin')
        container('maven') {
          withCredentials([kubeconfigContent(credentialsId: 'sangomall-kubeconfig', variable: 'KUBECONFIG_CONTENT')]) {
            sh '''# 1. 创建.kube目录（-p避免目录已存在时报错）
mkdir -p ~/.kube

# 2. 写入KubeConfig（去掉多余引号，保持原格式）
echo "$KUBECONFIG_CONTENT" > ~/.kube/config

# 3. 安装kubectl（容器内默认没有，以alpine系统为例，适配maven容器）
apk add --no-cache kubectl || true

# 4. 替换变量并部署（用大括号包裹PROJECT_NAME避免变量解析错误）
envsubst < "${PROJECT_NAME}/deploy/deploy.yaml" | kubectl apply -f -'''
          }

        }

      }
    }

  }
  
  
}
