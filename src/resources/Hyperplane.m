%unit plane
clc,close,clear
mop.name='DTLZ1';
mop.obj_nums=3;
R='R9';
color=[0.376 0.376 0.376];
limit=0.5;
A=[0 limit 0];
B=[limit 0 0];
C=[0 0 limit];
[x,y]=meshgrid(0:limit/10:limit);alpha(0.001);
z=limit-x-y;
c=x+y>limit;
z(c)=nan;
x(c)=nan;
y(c)=nan;
a=mesh(x,y,z);
set(a,'EdgeColor',color);
line(A,B,C,'color',color); 
line(C,B,A,'color',color);hold on;
limit=0.6;
A=[0 0 limit];
B=[limit 0 0];
C=[0 0 0];

LineWidth=1.5;
plot3(A,B,C,'color',color,'LineWidth',LineWidth); hold on;
line([0 0],[0 0 ],[0 limit],'color',color,'LineWidth',LineWidth); hold on;

S=load(strcat('pf_solution/','PFs_',mop.name,'(',int2str(mop.obj_nums),')_',R,'.dat'));     
C = repmat([1 .75 .5],size(S,1),1);
scatter3(S(:,1),S(:,2),S(:,3),70,'o','filled','MarkerFaceColor',[0.376 0.376 0.376],'MarkerEdgeColor',[0.376 0.376 0.376]);
%scatter3(S(1,1),S(1,2),S(1,3),[0.376,0.376,0.376],'filled');
%scatter3(S(:,1),S(:,2),S(:,3),25,'MarkerFaceColor',[0.376 0.376 0.376]);
axis([0 limit 0 limit 0 limit]);
xlabel('f1');                                                         
ylabel('f2');
zlabel('f3');
grid off;