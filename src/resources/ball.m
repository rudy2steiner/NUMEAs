clear
mop.name='DTLZ2';
mop.obj_nums=3;
R='R9';
a=20;
r=1;
color=[0.376 0.376 0.376];
u=0:pi/a:2*pi;
v=0:pi/a:2*pi;
[U,V]=meshgrid(u,v); 
x=r*sin(U).*cos(V);
y=r*sin(U).*sin(V);
z=r*cos(U);
a=mesh(abs(x),abs(y),abs(z));alpha(0.001);hold on;
set(a,'EdgeColor',color);
limit=1.2;
A=[0 0 limit];
B=[limit 0 0];
C=[0 0 0];

LineWidth=1.5;
plot3(A,B,C,'color',color,'LineWidth',LineWidth); hold on;
line([0 0],[0 0 ],[0 limit],'color',color,'LineWidth',LineWidth); hold on;

C = repmat([1 .75 .5],size(S,1),1);
scatter3(S(:,1),S(:,2),S(:,3),70,'o','filled','MarkerFaceColor',[0.376 0.376 0.376],'MarkerEdgeColor',[0.376 0.376 0.376]);
axis([0 limit 0 limit 0 limit]);
grid off;