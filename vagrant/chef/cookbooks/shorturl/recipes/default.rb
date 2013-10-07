include_recipe "mysql::server"

execute "generate input file" do
  command "echo \"CREATE DATABASE IF NOT EXISTS shorturl;\nGRANT *.* \" > databases.sql"
end

execute "create database" do
  command "mysql -h localhost -u root --password=#{node['mysql']['server_root_password']} < databases.sql"
end
