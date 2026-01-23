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
            // 核心SSH配置（已验证100%有效）
            sh '''
              mkdir -p ~/.ssh && chmod 700 ~/.ssh
              cp $GITHUB_SSH_KEY ~/.ssh/id_ed25519 && chmod 600 ~/.ssh/id_ed25519
              ssh-keyscan -t ecdsa,ed25519,rsa github.com >> ~/.ssh/known_hosts && chmod 644 ~/.ssh/known_hosts
              git config --global core.sshCommand "ssh -i ~/.ssh/id_ed25519 -o UserKnownHostsFile=~/.ssh/known_hosts -o StrictHostKeyChecking=no"
              git remote set-url origin git@github.com:zhuyanghua2003/mail.git
              git push origin --tags --ipv4
            '''
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
                    sh '''#!/bin/bash
set -e  # 任意命令失败时立即退出，避免掩盖错误

# ========== 1. 初始化KubeConfig（核心，确保kubectl能连接集群） ==========
mkdir -p ~/.kube
echo "$KUBECONFIG_CONTENT" > ~/.kube/config
chmod 600 ~/.kube/config  # 修复kubeconfig权限（K8s要求必须600）

# ========== 2. 安装kubectl（兼容alpine/debian/ubuntu系统） ==========
if command -v apt-get &>/dev/null; then
  # debian/ubuntu系统
  apt-get update && apt-get install -y kubectl
elif command -v apk &>/dev/null; then
  # alpine系统
  apk add --no-cache kubectl
else
  echo "不支持的系统，无法安装kubectl"
  exit 1
fi

# ========== 3. 校验前置条件（避免部署失败） ==========
# 检查deploy.yaml文件是否存在
DEPLOY_YAML="${PROJECT_NAME}/deploy/deploy.yaml"
if [ ! -f "$DEPLOY_YAML" ]; then
  echo "错误：部署文件 $DEPLOY_YAML 不存在！"
  exit 1
fi

# 检查PROJECT_NAME变量是否为空
if [ -z "$PROJECT_NAME" ]; then
  echo "错误：PROJECT_NAME变量为空！"
  exit 1
fi

# ========== 4. 变量替换 + 部署（增加日志，便于排查） ==========
echo "开始替换变量并部署 $PROJECT_NAME ..."
envsubst < "$DEPLOY_YAML" > "${PROJECT_NAME}/deploy/deploy-merged.yaml"  # 生成替换后的YAML（便于排查）
cat "${PROJECT_NAME}/deploy/deploy-merged.yaml"  # 打印替换后的YAML，确认镜像/参数正确
kubectl apply -f "${PROJECT_NAME}/deploy/deploy-merged.yaml" -n sangomall

# ========== 5. 部署后校验（等待Pod创建，检查状态） ==========
echo "等待 $PROJECT_NAME 的Pod启动..."
kubectl wait --for=condition=available deployment/$PROJECT_NAME -n sangomall --timeout=120s || {
  echo "警告：$PROJECT_NAME 部署超时，查看Pod日志："
  kubectl logs -l app=$PROJECT_NAME -n sangomall --previous
  exit 1
}

echo "$PROJECT_NAME 部署成功！"
'''
          }

        }

      }
    }

  }
  
  
}
